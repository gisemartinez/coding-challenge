package co.theorg.api.schema

import cats.effect._
import co.theorg.api.AppContext
import co.theorg.api.schema.Models.OrgChart
import co.theorg.db
import sangria.schema._

object Query {
  lazy val levelArg: Argument[Int] =
    Argument(
      name = "level",
      argumentType = IntType
    )

  def apply[F[_]: Effect]: ObjectType[AppContext, Unit] =
    ObjectType(
      name = "Query",
      fields = fields(
        Field(
          name = "loadOrgChart",
          fieldType = OptionType(OrgChart.Type),
          description = Some("Returns all org chart nodes"),
          arguments = List(levelArg),
          resolve = ctx => {
            db.OrgChartRepo(appContext = ctx.ctx)
              .loadOrgChart(ctx.arg(levelArg))
              .unsafeToFuture()
          }
        )
      )
    )
}
