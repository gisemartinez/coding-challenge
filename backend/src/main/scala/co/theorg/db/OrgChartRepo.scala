package co.theorg.db

import cats.effect.IO
import co.theorg.api.AppContext
import co.theorg.api.schema.Models.{
  AddNodeInput,
  AddNodeResult,
  OrgChart,
  OrgChartNode
}
import co.theorg.db.Tables._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

final case class OrgChartRepo(appContext: AppContext) {

  /**   - If level = 0 , return all nodes
    *   - If level = 1 , return all but the root nodes
    *   - If level = n , return all nodes equal to or below level n
    */
  def loadOrgChart(level: Int) = {
    level match {
      case 0 =>
        appContext
          .read(ChartNode.to[List].result)
          .map(rows => OrgChart(rows.map(fromRow)))
      case 1 =>
        // filter nodes without parent
        appContext
          .read(ChartNode.filter(_.parentid.nonEmpty).to[List].result)
          .map(rows => OrgChart(rows.map(fromRow)))

      case n =>
        // all nodes have a level associated
        appContext
          .read(ChartNode.filter(_.level < n).to[List].result)
          .map(rows => OrgChart(rows.map(fromRow)))
    }
  }

  /** When inserting check if [[input]] has a parent. If it has a parent, then
    * use its level to calculate what's the level of this new node. If it
    * doesn't have a parent, then node is on the root of the chart
    */
  def addNode(input: AddNodeInput): IO[Option[AddNodeResult]] = {
    val newLevelQ = input.parentId match {
      case Some(nodeParentId) =>
        // Find existing parent node to calculate level using parent level + 1
        ChartNode.filter(_.id === nodeParentId).map(_.level + 1).to[List].result
      case None =>
        // When parent is not given, then it's a root level (0)
        DBIO.successful(List(0))
    }

    val siblingNodesQ = ChartNode
      .filterIf(input.parentId.nonEmpty)(n => n.parentid === input.parentId)
      .to[List]
      .result

    implicit val ec = appContext.executionContext
    val addRowsQ = for {
      level <- newLevelQ
      siblings <- siblingNodesQ
      canAddSiblings = siblings.size < appContext.maxChildNodesPerParent
      _ <- level.headOption match {
        case Some(0) =>
          // allow multiple ceos
          ChartNode += toRow(input, 0)
        case Some(n) if canAddSiblings =>
          ChartNode += toRow(input, n)
        case _ =>
          DBIO.failed(new Throwable("New node can't be saved"))
      }
    } yield ()

    appContext
      .write(addRowsQ)
      .flatMap(_ =>
        loadOrgChart(0)
          .map(updatedChart => Some(AddNodeResult(Some(updatedChart))))
      )
  }

  private def fromRow(r: ChartNodeRow): OrgChartNode = {
    OrgChartNode(r.id, r.title, r.parentid, r.level)
  }

  private def toRow(input: AddNodeInput, level: Int) =
    ChartNodeRow(UUID.randomUUID(), input.parentId, input.title, level)
}
