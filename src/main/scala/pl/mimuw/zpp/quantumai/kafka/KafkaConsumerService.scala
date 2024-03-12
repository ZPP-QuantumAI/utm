package pl.mimuw.zpp.quantumai.kafka

import pl.mimuw.zpp.quantumai.grade.GradingService
import pl.mimuw.zpp.quantumai.kafka.domain.GradeRequest
import zio._
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

trait KafkaConsumerService {
  def consume(): ZIO[Producer with Consumer, Throwable, Unit]
}

object KafkaConsumerService {
  def consume(): ZIO[KafkaConsumerService with Producer with Consumer, Throwable, Unit] =
    ZIO.serviceWithZIO[KafkaConsumerService](_.consume())
}

case class KafkaConsumerServiceImpl(gradingService: GradingService) extends KafkaConsumerService {
  override def consume(): ZIO[Producer with Consumer, Throwable, Unit] = {
    Consumer
      .plainStream(Subscription.topics("grade-requests"), Serde.string, GradeRequest.value)
      .mapZIO(record => {
        for {
          _ <- ZIO.logInfo(s"Received: ${record.record.value()}")
          _ <- gradingService.gradeRequest(record.record.value())
        } yield ()
      })
      .runDrain
  }
}

object KafkaConsumerServiceImpl {
  val layer: ZLayer[
    GradingService,
    Throwable,
    KafkaConsumerService
  ] = {
    ZLayer {
      for {
        gradingService <- ZIO.service[GradingService]
      } yield KafkaConsumerServiceImpl(gradingService)
    }
  }
}
