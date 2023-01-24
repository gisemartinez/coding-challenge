package co.theorg.api.schema

import co.theorg.api.AppContext
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Decoder, Encoder, Json}
import sangria.marshalling._
import sangria.schema._

import java.util.UUID

object Models {
  implicit val CirceResultMarshaller: circe.CirceResultMarshaller.type =
    circe.CirceResultMarshaller
  implicit val CirceMarshallerForType: circe.CirceMarshallerForType.type =
    circe.CirceMarshallerForType
  implicit val CirceInputUnmarshaller: circe.CirceInputUnmarshaller.type =
    circe.CirceInputUnmarshaller
  implicit val circeToInput: circe.circeToInput.type = circe.circeToInput
  implicit val circeFromInput: circe.circeFromInput.type = circe.circeFromInput

  implicit def circeEncoderToInput[T: Encoder]: ToInput[T, Json] =
    circe.circeEncoderToInput

  implicit def circeDecoderFromInput[T: Decoder]: FromInput[T] =
    circe.circeDecoderFromInput
  final case class AddNodeInput(title: String, parentId: Option[UUID])

  final case class AddNodeResult(orgChart: Option[OrgChart])

  final case class OrgChart(nodes: List[OrgChartNode])

  final case class OrgChartNode(
      id: UUID,
      title: String,
      parentId: Option[UUID],
      level: Int
  )

  object AddNodeInput {
    implicit val addNodeInputCodec: Codec[AddNodeInput] = deriveCodec
    val Type =
      InputObjectType[AddNodeInput](
        "AddNodeInput",
        List(
          InputField("title", StringType),
          InputField("parentId", OptionInputType(Utils.Uuid))
        )
      )
  }

  object AddNodeResult {
    implicit val addNodeResultCodec: Codec[AddNodeResult] = deriveCodec
    val Type = ObjectType[AppContext, AddNodeResult](
      name = "AddNodeResult",
      fields = fields[AppContext, AddNodeResult](
        Field("orgChart", OptionType(OrgChart.Type), resolve = _.value.orgChart)
      )
    )
  }

  object OrgChart {
    implicit val orgChartCodec: Codec[OrgChart] = deriveCodec

    val Type = ObjectType[AppContext, OrgChart](
      name = "OrgChart",
      fields = fields[AppContext, OrgChart](
        Field("nodes", ListType(OrgChartNode.Type), resolve = _.value.nodes)
      )
    )
  }

  object OrgChartNode {
    implicit val orgChartNodeCodec: Codec[OrgChartNode] = deriveCodec
    val Type = ObjectType[AppContext, OrgChartNode](
      name = "OrgChartNode",
      fields = fields[AppContext, OrgChartNode](
        Field("id", Utils.Uuid, resolve = _.value.id),
        Field("title", StringType, resolve = _.value.title),
        Field("parentId", OptionType(Utils.Uuid), resolve = _.value.parentId)
      )
    )
  }
}
