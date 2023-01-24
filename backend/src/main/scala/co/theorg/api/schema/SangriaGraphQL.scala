package co.theorg.api.schema

import cats.effect.Async
import cats.implicits._
import io.circe.optics.JsonPath.root
import io.circe.{Json, JsonObject}
import sangria.ast.Document
import sangria.execution.{ExceptionHandler, Executor, HandledException}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.schema.Schema

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final case class SangriaGraphQL[F[_], A](
    schema: Schema[A, Unit],
    userContext: A,
    blockingExecutionContext: ExecutionContext
)(implicit
    F: Async[F]
) {

  import SangriaGraphQL._

  /** Executes a JSON-encoded request in the standard POST encoding, described
    * thus in the spec:
    *
    * A standard GraphQL POST request should use the application/json content
    * type, and include a JSON-encoded body of the following form:
    *
    * { "query": "...", "operationName": "...", "variables": { "myVariable":
    * "someValue", ... } }
    *
    * `operationName` and `variables` are optional fields. `operationName` is
    * only required if multiple operations are present in the query.
    *
    * @return
    *   either an error Json or result Json
    */
  def query(request: Json): F[Either[Json, Json]] = {
    val queryString = queryStringLens.getOption(request)
    val operationName = operationNameLens.getOption(request)
    val variables =
      variablesLens.getOption(request).getOrElse(JsonObject())
    queryString match {
      case Some(qs) => query(qs, operationName, variables)
      case None =>
        fail(
          formatString("No 'query' property was present in the request.")
        )
    }
  }

  private def query(
      query: String,
      operationName: Option[String],
      variables: JsonObject
  ): F[Either[Json, Json]] =
    QueryParser.parse(query) match {
      case Success(ast) =>
        exec(schema, userContext, ast, operationName, variables)(
          blockingExecutionContext
        )
      case Failure(e) => fail(formatThrowable(e))
    }

  private def fail(j: Json): F[Either[Json, Json]] =
    F.pure(j.asLeft)

  /** Execute a GraphQL query with Sangria */
  private def exec(
      schema: Schema[A, Unit],
      ctx: A,
      query: Document,
      operationName: Option[String],
      variables: JsonObject
  )(implicit ec: ExecutionContext): F[Either[Json, Json]] =
    F.async { (cb: Either[Throwable, Json] => Unit) =>
      Executor
        .execute(
          schema = schema,
          queryAst = query,
          userContext = ctx,
          variables = Json.fromJsonObject(variables),
          operationName = operationName,
          exceptionHandler = ExceptionHandler { case (_, e) =>
            HandledException(e.getMessage)
          }
        )
        .onComplete {
          case Success(value) => cb(Right(value))
          case Failure(error) => cb(Left(error))
        }
    }.attempt
      .flatMap {
        case Right(json) => F.pure(json.asRight)
        case Left(err)   => fail(formatThrowable(err))
      }
}

/** A GraphQL implementation based on Sangria. */
object SangriaGraphQL {

  // Some circe lenses
  private val queryStringLens = root.query.string
  private val operationNameLens = root.operationName.string
  private val variablesLens = root.variables.obj

  // Format a String as a GraphQL `errors`
  private def formatString(s: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(s))))

  // Format a Throwable as a GraphQL `errors`
  private def formatThrowable(e: Throwable): Json = Json.obj(
    "errors" -> Json.arr(
      Json.obj(
        "class" -> Json.fromString(e.getClass.getName),
        "message" -> Json.fromString(e.getMessage)
      )
    )
  )
}
