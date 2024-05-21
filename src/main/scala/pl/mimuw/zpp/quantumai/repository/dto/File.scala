package pl.mimuw.zpp.quantumai.repository.dto

import org.bson.types.Binary

import java.nio.ByteBuffer

final case class File(_id: String, data: ByteBuffer)

object File {
  def apply(solutionId: String, data: String): File =
    File(solutionId, data)
}
