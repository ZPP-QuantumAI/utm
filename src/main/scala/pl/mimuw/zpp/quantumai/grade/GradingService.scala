package pl.mimuw.zpp.quantumai.grade

import pl.mimuw.zpp.quantumai.kafka.KafkaProducerService
import pl.mimuw.zpp.quantumai.kafka.domain.{GradeRequest, GradeResponse}
import pl.mimuw.zpp.quantumai.repository.dto.Graph.toInput
import pl.mimuw.zpp.quantumai.repository.{FileRepositoryService, GraphRepositoryService}
import zio._
import zio.kafka.producer.Producer

import java.lang.{System => Timer}
import java.util.Base64
import java.util.concurrent.TimeUnit
import scala.sys.process._

trait GradingService {
  def gradeRequest(gradeRequest: GradeRequest): ZIO[Producer, Throwable, Unit]
}

case class GradingServiceImpl(
    producerService: KafkaProducerService,
    fileRepositoryService: FileRepositoryService,
    graphRepositoryService: GraphRepositoryService
) extends GradingService {
  override def gradeRequest(gradeRequest: GradeRequest): ZIO[Producer, Throwable, Unit] = {
    for {
      file   <- fileRepositoryService.readFile(gradeRequest.solutionId)
      graphs <- graphRepositoryService.readGraphs(gradeRequest.requests.map(singleRequest => singleRequest.graphId))
      _ <- ZIO.foreachParDiscard(graphs.zip(gradeRequest.requests.map(x => x.gradeId))) { case (graph, gradeID) =>
        val pythonScript = new String(Base64.getDecoder.decode(file.data))
        val start        = Timer.currentTimeMillis()

        val gradeZio = for {
          res <- processOne(pythonScript, toInput(graph))
          end <- Clock.currentTime(TimeUnit.MILLISECONDS)
          _   <- producerService.produce(GradeResponse(gradeID, res._1, res._2, end - start))
        } yield ()

        gradeZio.catchAll { e =>
          val end = Timer.currentTimeMillis()
          producerService.produce(GradeResponse(gradeID, success = false, e.getMessage, end - start))
        }
      }
    } yield ()
  }

  private def processOne(pythonScript: String, input: String): ZIO[Any, Throwable, (Boolean, String)] = {
    val output = new StringBuilder
    val error  = new StringBuilder
    for {
      process <- ZIO.succeed(
        Process(s"python3 -c '$pythonScript' $input").run(
          ProcessLogger(line => output.append(line).append("\n"), line => error.append(line).append("\n"))
        )
      )
    } yield (process.exitValue() == 0, output.toString() + error.toString())
  }
}

object GradingServiceImpl {
  val layer: ZLayer[
    KafkaProducerService with Producer with FileRepositoryService with GraphRepositoryService,
    Throwable,
    GradingService
  ] = {
    ZLayer {
      for {
        graphRepositoryService <- ZIO.service[GraphRepositoryService]
        producerService        <- ZIO.service[KafkaProducerService]
        fileRepositoryService  <- ZIO.service[FileRepositoryService]
      } yield GradingServiceImpl(producerService, fileRepositoryService, graphRepositoryService)
    }
  }
}
