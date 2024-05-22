package pl.mimuw.zpp.quantumai.grade

import pl.mimuw.zpp.quantumai.kafka.KafkaProducerService
import pl.mimuw.zpp.quantumai.kafka.domain.{GradeRequest, GradeResponse, SingleGradeRequest}
import pl.mimuw.zpp.quantumai.repository.dto.Graph.toInput
import pl.mimuw.zpp.quantumai.repository.dto.{File => FileDto}
import pl.mimuw.zpp.quantumai.repository.{FileRepositoryService, GraphRepositoryService}
import zio._
import zio.kafka.producer.Producer

import java.io.{File, FileOutputStream}
import java.lang.{System => Timer}
import java.nio.file.{Files, Path}
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
      file <- fileRepositoryService.readFile(gradeRequest.solutionId)
      _    <- decodeZip(file)
      graphMap = gradeRequest.requests.map(sgr => (sgr.graphId, sgr.gradeId)).toMap
      graphs <- graphRepositoryService.readGraphs(graphMap.keySet.toList)
      _      <- ZIO.logInfo(s"file: ${file._id}, graphs: ${graphs.head.name}")
      _ <- ZIO.foreachDiscard(graphs) { graph =>
        val gradeID = graphMap(graph._id)
        val gradeZio = for {
          _   <- ZIO.logInfo(s"Running the solution for graph $graph in $gradeID")
          res <- processOne(toInput(graph))
          end <- Clock.currentTime(TimeUnit.MILLISECONDS)
          _   <- producerService.produce(GradeResponse(gradeID, res._1, res._2, end - res._3))
        } yield ()

        gradeZio.catchAll { e =>
          producerService.produce(
            GradeResponse(gradeID, success = false, e.getMessage, 0L)
          )
        }
      }
    } yield ()
  }

  private def decodeZip(file: FileDto): Task[Int] = {
    val zippedFile = File.createTempFile(file._id, ".zip")
    val fos        = new FileOutputStream(zippedFile)
    fos.write(file.data.array())
    fos.close()

    val exitValue = Seq("unzip", zippedFile.getPath).run().exitValue()

    ZIO.succeed(exitValue)
  }

  private def processOne(input: String): ZIO[Any, Throwable, (Boolean, String, Long)] = {
    val output = new StringBuilder
    val error  = new StringBuilder
    for {
      _ <- ZIO.succeed(Process("pip3 install -r requirements.txt").run().exitValue())
      start = Timer.currentTimeMillis()
      process <- ZIO.succeed(
        (Process(s"""echo "$input"""") #| Process(
          "timeout 300 python3 run.py"
        )).run(
          ProcessLogger(line => output.append(line), line => error.append(line).append("\n"))
        )
      )
    } yield (process.exitValue() == 0, output.toString() + error.toString(), start)
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
