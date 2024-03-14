package pl.mimuw.zpp.quantumai.kafka

import org.apache.kafka.clients.producer.RecordMetadata
import pl.mimuw.zpp.quantumai.kafka.domain.GradeResponse
import zio._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

trait KafkaProducerService {
  def produce(gradeResponse: GradeResponse): RIO[Any with Producer, RecordMetadata]
}

case class KafkaProducerServiceImpl() extends KafkaProducerService {
  override def produce(gradeResponse: GradeResponse): RIO[Producer, RecordMetadata] = {
    Producer
      .produce(
        "run-result",
        gradeResponse.gradeId,
        gradeResponse,
        Serde.string,
        GradeResponse.value
      )
      .tapBoth(
        e => ZIO.logError(s"Sending grade response failed: ${e.getMessage}"),
        _ => ZIO.logInfo(s"Sent grade response successfully: $gradeResponse")
      )
  }
}

object KafkaProducerServiceImpl {
  val layer: ZLayer[Any, Throwable, KafkaProducerService] =
    ZLayer.succeed(KafkaProducerServiceImpl())
}