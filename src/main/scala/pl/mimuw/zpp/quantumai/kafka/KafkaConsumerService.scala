package pl.mimuw.zpp.quantumai.kafka

import pl.mimuw.zpp.quantumai.kafka.domain.{GradeRequest, GradeResponse}
import zio._
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

trait KafkaConsumerService {
  def consume(): ZIO[Producer with Consumer, Throwable, Unit]
}

object KafkaConsumerService {
  def consume(): ZIO[KafkaConsumerService with  Producer with  Consumer, Throwable, Unit] =
    ZIO.serviceWithZIO[KafkaConsumerService](_.consume())
}

case class KafkaConsumerServiceImpl(producerService: KafkaProducerService) extends KafkaConsumerService {
  override def consume(): ZIO[Producer with Consumer, Throwable, Unit] = {
    Consumer
      .plainStream(Subscription.topics("grade-requests"), Serde.string, GradeRequest.value)
      .mapZIO(record => {
        for {
          _ <- ZIO.logInfo(s"Received: ${record.record.value()}")
          gr = GradeResponse("xd", success = true, "xd", 1L)
          _ <- producerService.produce(gr)
        } yield ()
      })
      .runDrain
  }
}

object KafkaConsumerServiceImpl {
  val layer: ZLayer[KafkaProducerService with Producer with Consumer, Throwable, KafkaConsumerService] = {
    ZLayer {
      for {
        producerService <- ZIO.service[KafkaProducerService]
      } yield KafkaConsumerServiceImpl(producerService)
    }
  }
}

