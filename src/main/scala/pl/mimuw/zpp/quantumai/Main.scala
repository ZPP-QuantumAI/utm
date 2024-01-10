package pl.mimuw.zpp.quantumai

import pl.mimuw.zpp.quantumai.kafka._
import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.kafka.producer.{Producer, ProducerSettings}

object Main extends ZIOAppDefault {

  private val consumerLayer: ZLayer[Any, Throwable, Consumer] =
    ZLayer.scoped(Consumer.make(ConsumerSettings(List("localhost:9092")).withGroupId("utm")))

  private val producerLayer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(List("localhost:9092"))))

  private val app =
   KafkaConsumerService.consume()

  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(
      KafkaConsumerServiceImpl.layer,
      KafkaProducerServiceImpl.layer,
      consumerLayer,
      producerLayer
    )
}
