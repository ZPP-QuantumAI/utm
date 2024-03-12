package pl.mimuw.zpp.quantumai.kafka.domain

import zio._
import zio.json._
import zio.kafka.serde.Serde

final case class GradeRequest(requests: Seq[SingleGradeRequest], solutionId: String)
final case class SingleGradeRequest(gradeId: String, graphId: String)

object GradeRequest {
  implicit val encoder: JsonEncoder[GradeRequest] =
    DeriveJsonEncoder.gen[GradeRequest]

  implicit val decoder: JsonDecoder[GradeRequest] =
    DeriveJsonDecoder.gen[GradeRequest]

  val value: Serde[Any, GradeRequest] =
    Serde.string.inmapM[Any, GradeRequest](s =>
      ZIO
        .fromEither(s.fromJson[GradeRequest])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))
}

object SingleGradeRequest {
  implicit val encoder: JsonEncoder[SingleGradeRequest] =
    DeriveJsonEncoder.gen[SingleGradeRequest]

  implicit val decoder: JsonDecoder[SingleGradeRequest] =
    DeriveJsonDecoder.gen[SingleGradeRequest]

  val value: Serde[Any, SingleGradeRequest] =
    Serde.string.inmapM[Any, SingleGradeRequest](s =>
      ZIO
        .fromEither(s.fromJson[SingleGradeRequest])
        .mapError(e => new RuntimeException(e))
    )(r => ZIO.succeed(r.toJson))
}
