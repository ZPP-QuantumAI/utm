package pl.mimuw.zpp.quantumai.repository.dto

import org.bson.types.Binary

final case class File(_id: String, data: Binary)

object File {
  def apply(solutionId: String, data: String): File =
    File(solutionId, data)
}
