package pl.mimuw.zpp.quantumai.kafka.domain

import zio.ZIO
import zio.json._
import zio.kafka.serde.Serde

case class GradeResponse(gradeId: String, success: Boolean, output: String, runtimeInMs: Long)

object GradeResponse {
  implicit val encoder: JsonEncoder[GradeResponse] =
    DeriveJsonEncoder.gen[GradeResponse]

  implicit val decoder: JsonDecoder[GradeResponse] =
    DeriveJsonDecoder.gen[GradeResponse]

  val value: Serde[Any, GradeResponse] =
    Serde.string.inmapM[Any, GradeResponse](s =>
      ZIO.fromEither(s.fromJson[GradeResponse])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))
}