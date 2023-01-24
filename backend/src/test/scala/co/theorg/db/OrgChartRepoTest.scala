package co.theorg.db

import cats.effect._
import co.theorg.api.AppContext
import co.theorg.api.schema.Models
import co.theorg.api.schema.Models.AddNodeInput
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.ExecutionContext

class OrgChartRepoTest
    extends AnyFreeSpec
    with BeforeAndAfterEach
    with Matchers {
  val db = Database.forConfig("postgres")
  val applicationConf = ConfigFactory.load("application.conf")

  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  val appContext = AppContext(db, 1, ExecutionContext.global)(contextShift)

  override def afterEach(): Unit = {
    super.afterEach()
    appContext.write(Tables.ChartNode.delete).unsafeRunSync()
  }

  "When adding nodes" - {
    "it should allow to have many Ceos" in {
      for {
        _ <- OrgChartRepo(appContext).addNode(AddNodeInput("CEO", None))
        _ <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Co-CEO", None))
          .map(_.toList)
        result <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Co-Co-CEO", None))
          .map(_.toList)
      } yield {
        assert(result.nonEmpty)
        val titles =
          result.flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.title)))
        val levels =
          result.flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.level)))
        assert(titles.nonEmpty)
        assert(levels.nonEmpty)
        titles should contain theSameElementsAs List(
          "CEO",
          "Co-CEO",
          "Co-Co-CEO"
        )
        levels should contain theSameElementsAs List(0, 0, 0)
      }
    }.unsafeRunSync()

    "it should allow to have at least one child per level" in {
      for {
        rootResult <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("CEO", None))
          .map(_.toList)
        rootId = rootResult
          .flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.id)))
          .headOption
        anotherNodeResult <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Level 1", rootId))
          .map(_.toList)
      } yield {
        assert(anotherNodeResult.nonEmpty)
        val titles = anotherNodeResult.flatMap(
          _.orgChart.toList.flatMap(_.nodes.map(_.title))
        )
        val levels = anotherNodeResult.flatMap(
          _.orgChart.toList.flatMap(_.nodes.map(_.level))
        )
        assert(titles.nonEmpty)
        assert(levels.nonEmpty)
        titles should contain theSameElementsAs List("CEO", "Level 1")
        levels should contain theSameElementsAs List(0, 1)
      }
    }.unsafeRunSync()

    "it should fail when adding too many nodes in a level" in {
      for {
        rootResult <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("CEO", None))
          .map(_.toList)
        rootId = rootResult
          .flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.id)))
          .headOption
        allowed <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Level 1", rootId))
          .map(_.toList)
        disallowedVV <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("A too many node", rootId))
          .map(_.toList)
          .attempt
      } yield {
        assert(allowed.nonEmpty)
        val titles =
          allowed.flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.title)))
        val levels =
          allowed.flatMap(_.orgChart.toList.flatMap(_.nodes.map(_.level)))
        assert(titles.nonEmpty)
        assert(levels.nonEmpty)
        titles should contain theSameElementsAs List("CEO", "Level 1")
        levels should contain theSameElementsAs List(0, 1)
        assert(disallowedVV.isLeft)
        assert(
          disallowedVV.swap.forall(_.getMessage == "New node can't be saved")
        )
      }
    }.unsafeRunSync()
  }

  "When listing nodes" - {
    def dataIO()
        : IO[(Option[UUID], Option[UUID], Option[UUID], Option[UUID])] = {
      for {
        firstOrg <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("CEO", None))
          .map(_.toList)
        ceoId = findId(firstOrg, "CEO")
        secondOrg <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Director", ceoId))
          .map(_.toList)
        directorId = findId(secondOrg, "Director")
        thirdOrg <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Principal Dev", directorId))
          .map(_.toList)
        principalDevId = findId(thirdOrg, "Principal Dev")
        forthOrg <- OrgChartRepo(appContext)
          .addNode(AddNodeInput("Jr Dev", principalDevId))
          .map(_.toList)
        jrDevId = findId(forthOrg, "Jr Dev")
      } yield (ceoId, directorId, principalDevId, jrDevId)
    }

    "list all" in {
      for {
        data <- dataIO()
        (ceo, director, principalDev, jrDev) = data
        orgChart <- OrgChartRepo(appContext).loadOrgChart(0)
      } yield {
        orgChart.nodes.map(_.id) should contain theSameElementsAs List(
          ceo,
          director,
          principalDev,
          jrDev
        ).flatten
      }
    }.unsafeRunSync()

    "list only roots" in {
      for {
        data <- dataIO()
        (_, director, principalDev, jrDev) = data
        orgChart <- OrgChartRepo(appContext).loadOrgChart(1)
      } yield {
        orgChart.nodes.map(_.id) should contain theSameElementsAs List(
          director,
          principalDev,
          jrDev
        )
      }
    }.unsafeRunSync()

    "list below n" in {
      for {
        data <- dataIO()
        (ceo, director, principalDev, jrDev) = data
        orgChart <- OrgChartRepo(appContext).loadOrgChart(2)
      } yield {
        orgChart.nodes.map(_.id) should contain theSameElementsAs List(
          ceo,
          director,
          principalDev
        )
      }
    }.unsafeRunSync()
  }

  /** Helper to find a node id that can be used as a parent of another node */
  private def findId(root: List[Models.AddNodeResult], title: String) = {
    root
      .flatMap(_.orgChart.flatMap(_.nodes.find(_.title == title)))
      .headOption
      .map(_.id)
  }
}
