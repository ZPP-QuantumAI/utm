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
import scala.reflect.io.Directory
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
      _    <- ZIO.logInfo(s"read file from repository with id ${gradeRequest.solutionId}")
      _    <- decodeZip(file)
      _    <- ZIO.logInfo(s"Decoded zip to repository")
      graphMap = gradeRequest.requests.map(sgr => (sgr.graphId, sgr.gradeId)).toMap
      graphs <- graphRepositoryService.readGraphs(graphMap.keySet.toList)
      _ <- ZIO.foreachDiscard(graphs) { graph =>
        val gradeID = graphMap(graph._id)
        val gradeZio = for {
          _   <- ZIO.logInfo(s"Running the solution for graph $graph in $gradeID")
          res <- processOne(toInput(graph))
          _   <- ZIO.logInfo(s"Finished grading the solution for graph $graph in $gradeID")
          end <- Clock.currentTime(TimeUnit.MILLISECONDS)
          _   <- producerService.produce(GradeResponse(gradeID, res._1, res._2, end - res._3))
        } yield ()

        gradeZio.catchAll { e =>
          for {
            _ <- producerService.produce(
              GradeResponse(gradeID, success = false, e.getMessage, 0L)
            )
          } yield ()
        }
      }
      _ <- clearRoot()
    } yield ()
  }

  private def clearRoot(): Task[Unit] = {
    Process("find . -mindepth 1 ! -name 'your-app.jar' -exec rm -rf {} +").run().exitValue()
    ZIO.succeed()
  }

  private def decodeZip(file: FileDto): Task[Unit] = {
    val zippedFile = File.createTempFile(file._id, ".zip")
    val fos        = new FileOutputStream(zippedFile)
    fos.write(file.data.array())
    fos.close()

    Process(s"unzip -o ${zippedFile.getPath}").run().exitValue()

    ZIO.succeed()
  }

  private def processOne(input: String): ZIO[Any, Throwable, (Boolean, String, Long)] = {
    val output = new StringBuilder
    val error  = new StringBuilder
    for {
      _ <- ZIO.succeed(Process(s"pip3 install -r requirements.txt").run().exitValue())
      start = Timer.currentTimeMillis()
      process <- ZIO.succeed(
        (Process(s"""echo "$input"""") #| Process(
          s"timeout 300 python3 run.py"
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
