package pl.mimuw.zpp.quantumai.kafka

import pl.mimuw.zpp.quantumai.kafka.domain.GradeRequest
import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde

trait KafkaConsumerService {
  def run(): ZIO[Any, Throwable, Unit]
}

object KafkaConsumerService {
  def run(): ZIO[KafkaConsumerService, Throwable, Unit] =
    ZIO.serviceWithZIO(_.run())
}

case class KafkaConsumerServiceImpl() extends KafkaConsumerService {
  private val consumerLayer: ZLayer[Any, Throwable, Consumer] =
    ZLayer.scoped(Consumer.make(ConsumerSettings(List("localhost:9092")).withGroupId("utm")))

  override def run(): ZIO[Any, Throwable, Unit] = {
    Consumer
      .plainStream(Subscription.topics("grade-requests"), Serde.string, GradeRequest.value)
      .map(record => println(s"Received: ${record.record.value()}"))
      .runDrain
      .provideSomeLayer(consumerLayer)
  }
}

object KafkaConsumerServiceImpl {
  val layer: ZLayer[Any, Throwable, KafkaConsumerService] = {
    ZLayer.succeed(KafkaConsumerServiceImpl())
  }
}

