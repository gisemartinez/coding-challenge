package co.theorg.api.schema

import sangria.schema._
import sangria.validation.ValueCoercionViolation

import java.util.UUID
import scala.util._

object Utils {
  case object UuidCoercionViolation
      extends ValueCoercionViolation(
        "UUID value expected in format [00000000-0000-0000-0000-000000000000]."
      )

  private def parseUuid(s: String) = Try(UUID.fromString(s)) match {
    case Success(s) =>
      Right(s)
    case Failure(_) =>
      Left(UuidCoercionViolation)
  }

  implicit val Uuid = ScalarType[UUID](
    "Uuid",
    coerceOutput = (s, _) => s.toString,
    coerceUserInput = {
      case s: String => parseUuid(s)
      case _ =>
        Left(UuidCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _, _, _) => parseUuid(s)
      case _ =>
        Left(UuidCoercionViolation)
    }
  )
}
