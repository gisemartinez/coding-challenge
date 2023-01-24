package co.theorg.api

import cats.effect.{
  Blocker,
  ConcurrentEffect,
  ContextShift,
  Effect,
  ExitCode,
  IO,
  IOApp,
  Resource,
  Timer
}
import co.theorg.api.schema.{GraphQL, GraphQLRoutes, SangriaGraphQL}
import com.typesafe.config.ConfigFactory
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import sangria.schema.Schema
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  val db = Database.forConfig("postgres")
  val applicationConf = ConfigFactory.load("application.conf")

  // Construct a GraphQL implementation based on our Sangria definitions.
  def graphQL[F[_]: Effect: ContextShift: Logger](
      blockingContext: ExecutionContext
  ): SangriaGraphQL[F, AppContext] =
    SangriaGraphQL[F, AppContext](
      Schema(
        query = schema.Query[F],
        mutation = Some(schema.Mutation[F])
      ),
      AppContext(
        db,
        applicationConf.getInt("theorg.maxChildNodesPerParent"),
        blockingContext
      ),
      blockingContext
    )

  // Resource that mounts the given `routes` and starts a server.
  def server[F[_]: ConcurrentEffect: ContextShift: Timer](
      routes: HttpRoutes[F]
  ): Resource[F, Server[F]] =
    BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .resource

  // Resource that constructs our final server.
  def resource[F[_]: ConcurrentEffect: ContextShift: Timer](implicit
      L: Logger[F]
  ): Resource[F, Server[F]] =
    for {
      b <- Blocker[F]
      gql = graphQL[F](b.blockingContext)
      rts = GraphQLRoutes[F](gql)
      svr <- server[F](rts)
    } yield svr

  // Our entry point starts the server and blocks forever.
  def run(args: List[String]): IO[ExitCode] = {
    implicit val log = Slf4jLogger.getLogger[IO]
    resource[IO].use(_ => IO.never.as(ExitCode.Success))
  }

}
