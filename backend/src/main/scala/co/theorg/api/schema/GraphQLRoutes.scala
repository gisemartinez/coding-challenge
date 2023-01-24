package co.theorg.api.schema

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import co.theorg.api.AppContext
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object GraphQLRoutes {

  /** An `HttpRoutes` that maps the standard `/graphql` path to a `GraphQL`
    * instance.
    */
  def apply[F[_]: Sync: ContextShift](
      graphQL: SangriaGraphQL[F, AppContext]
  ): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F];
    import dsl._
    HttpRoutes.of[F] { case req @ POST -> Root / "graphql" =>
      req.as[Json].flatMap(graphQL.query).flatMap {
        case Right(json) => Ok(json)
        case Left(json)  => BadRequest(json)
      }
    }
  }
}
