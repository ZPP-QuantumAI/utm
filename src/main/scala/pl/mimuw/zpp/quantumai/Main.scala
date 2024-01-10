package pl.mimuw.zpp.quantumai

import pl.mimuw.zpp.quantumai.kafka._
import zio._

object Main extends ZIOAppDefault {

  private val myAppLayer: ZLayer[zio.Scope, Throwable, KafkaConsumerService] =
    KafkaConsumerServiceImpl.layer

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      _ <- KafkaConsumerService.run()
    } yield ()
  }.provideLayer(myAppLayer)
}
