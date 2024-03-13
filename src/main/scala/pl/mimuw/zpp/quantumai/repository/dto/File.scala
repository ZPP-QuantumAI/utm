package pl.mimuw.zpp.quantumai.repository.dto

import org.mongodb.scala.bson.ObjectId

final case class File(_id: ObjectId, solutionId: String, data: String)

object File {
  def apply(solutionId: String, data: String): File =
    File(new ObjectId(), solutionId, data)
}
