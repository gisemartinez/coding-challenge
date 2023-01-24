package co.theorg.api

import cats.effect.{ContextShift, IO}
import co.theorg.api.AppContext._
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlAction

import scala.concurrent.ExecutionContext

final case class AppContext(
    database: Database,
    maxChildNodesPerParent: Int,
    executionContext: ExecutionContext
)(implicit cs: ContextShift[IO]) {
  def write[T](action: Write[T]): IO[T] =
    IO.fromFuture(IO(database.run(action)))

  def read[T](action: Read[T]): IO[T] =
    IO.fromFuture(IO(database.run(action)))
}

object AppContext {
  type Write[+R] = DBIOAction[R, NoStream, Effect.All]
  type Read[+R] = SqlAction[R, NoStream, Effect.Read]
}
