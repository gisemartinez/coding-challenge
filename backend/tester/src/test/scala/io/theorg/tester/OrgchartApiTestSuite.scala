package io.theorg.tester

import caliban.client.Operations.{RootMutation, RootQuery}
import caliban.client.SelectionBuilder
import sttp.capabilities
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import sttp.model.Uri
import zio.test.Assertion._
import zio.test.TestAspect.sequential
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Managed, Task, ZIO}
import io.theorg.tester._
import io.theorg.tester.client.backend._
import java.util.UUID
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZManaged
import zio.Ref

object OrgchartApiTestSuite extends DefaultRunnableSpec {

  private val MaxNodesPerLevel = 3
  private val AddNodeConcurrency = 4
  private val MaxAddNodeRequests = 1000
  private val targetGraphqlUrl = "http://localhost:8080/graphql"

  final case class OrgChartNodeMapped(
      id: UUID,
      title: String,
      parentId: Option[UUID]
  )
  private val apiUrl: Uri =
    Uri.parse(targetGraphqlUrl).toOption.get

  def withServer(
      test: SttpBackend[Task, ZioStreams with WebSockets] => ZIO[
        TestEnvironment,
        Throwable,
        TestResult
      ]
  ): ZIO[TestEnvironment, Throwable, TestResult] = {
    val testJob =
      for {
        backend <- HttpClientZioBackend.managed()
        response <- Managed.fromEffect(test(backend))
      } yield response

    testJob.useNow
  }

  val orgChartSelection = OrgChart.nodes(
    (OrgChartNode.id.map(
      UUID.fromString
    ) ~ OrgChartNode.title ~ OrgChartNode.parentId.map(
      (_.map(UUID.fromString))
    )).mapN(OrgChartNodeMapped(_, _, _))
  )

  def getNodes(level: Int = 0) = Query.loadOrgChart(level)(
    orgChartSelection
  )

  def addNode(title: String, parentId: Option[UUID]) = {
    Mutation.addNode(AddNodeInput(title, parentId.map(_.toString())))(
      AddNodeResult.orgChart(orgChartSelection)
    )
  }

  private val calibanServerSpec =
    suite("Caliban server Spec")(
      testM("Empty on first run") {
        withServer { backend =>
          val response = getNodes().toRequest(apiUrl).send(backend)
          assertM(response.map(_.code.code))(equalTo(200))
          assertM(response.map(_.body).absolve.map(_.flatten.map(_.size)))(
            equalTo(Some(0))
          )
        }
      },
      testM("Concurrent updates don't break the requirements") {
        withServer { backend =>
          for {
            stateRef <- Ref.make(Set(Option.empty[UUID]))
            _ <- ZIO.foreachParN_(AddNodeConcurrency)(0 to MaxAddNodeRequests)(
              _ => {
                stateRef.get
                  .map(state => {
                    val n = util.Random.nextInt(state.size)
                    state.iterator.drop(n).next
                  })
                  .flatMap(nextParent =>
                    addNode("node title", nextParent)
                      .toRequest(apiUrl)
                      .send(backend)
                      .map(_.body)
                      .absolve
                      .map(_.flatten.flatten)
                      .map(
                        _.map(_.map(_.parentId))
                          .getOrElse(List(Option.empty[UUID]))
                          .distinct
                          .toSet
                      )
                      .flatMap(latestState => stateRef.update(_ => latestState))
                      .ignore
                  )

              }
            )
            response <- getNodes().toRequest(apiUrl).send(backend)
          } yield {
            assert(response.code.code)(equalTo(200))

            val responseNodes = response.body
              .map(_.flatten)
              .map(_.getOrElse(List.empty))
              .getOrElse(List.empty)

            val nodesOverThresholdByParent =
              responseNodes
                .groupBy(_.parentId)
                .filter(_._2.size > MaxNodesPerLevel)
            assert(responseNodes.size)(isGreaterThan(1))
            assert(nodesOverThresholdByParent.size)(equalTo(0))
          }
        }
      }
    ) @@ sequential

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Orgchart API Spec")(
      calibanServerSpec
    )
}
