package co.theorg.api.schema

import cats.effect._
import co.theorg.api.AppContext
import co.theorg.api.schema.Models.{AddNodeInput, AddNodeResult, OrgChart}
import co.theorg.db
import io.circe._
import sangria.schema._

object Mutation {
  lazy val addNodeInputArg = Argument("addNodeInput", AddNodeInput.Type)

  def apply[F[_]: Effect]: ObjectType[AppContext, Unit] =
    ObjectType(
      name = "Mutation",
      fields = fields(
        Field(
          name = "addNode",
          fieldType = OptionType(AddNodeResult.Type),
          description = Some("Adds a node to the org chart"),
          arguments = List(addNodeInputArg),
          resolve = ctx => {
            db.OrgChartRepo(appContext = ctx.ctx)
              .addNode(ctx.arg(addNodeInputArg))
              .unsafeToFuture()
          }
        )
      )
    )
}
