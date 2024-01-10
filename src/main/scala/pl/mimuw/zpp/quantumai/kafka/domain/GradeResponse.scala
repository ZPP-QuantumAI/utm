package pl.mimuw.zpp.quantumai.kafka.domain

case class GradeResponse(gradeId: String, success: Boolean, output: String, runtimeInMs: Long)
