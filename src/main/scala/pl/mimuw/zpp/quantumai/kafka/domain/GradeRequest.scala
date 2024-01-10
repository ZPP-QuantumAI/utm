package pl.mimuw.zpp.quantumai.kafka.domain

import zio._
import zio.json._
import zio.kafka.serde.Serde

case class GradeRequest(gradeId: String, graphIds: Seq[String], solutionId: String)

object GradeRequest {
  implicit val encoder: JsonEncoder[GradeRequest] =
    DeriveJsonEncoder.gen[GradeRequest]

  implicit val decoder: JsonDecoder[GradeRequest] =
    DeriveJsonDecoder.gen[GradeRequest]

  val value: Serde[Any, GradeRequest] =
    Serde.string.inmapM[Any, GradeRequest](s =>
      ZIO.fromEither(s.fromJson[GradeRequest])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))
}
