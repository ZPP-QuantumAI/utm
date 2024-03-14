package pl.mimuw.zpp.quantumai.repository

import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal
import pl.mimuw.zpp.quantumai.repository.dto.File
import zio.{ZIO, ZLayer}

trait FileRepositoryService {
  def readFile(solutionId: String): ZIO[Any, Throwable, File]
}

case class FileRepositoryServiceImpl(collection: MongoCollection[File]) extends FileRepositoryService {
  override def readFile(solutionId: String): ZIO[Any, Throwable, File] = {
    ZIO.fromFuture[File] { _ =>
      collection.find(equal("_id", solutionId)).first().toFuture()
    }
  }
}

object FileRepositoryServiceImpl {
  val layer: ZLayer[MongoCollection[File], Nothing, FileRepositoryService] = {
    ZLayer {
      for {
        mongoClient <- ZIO.service[MongoCollection[File]]
      } yield FileRepositoryServiceImpl(mongoClient)
    }
  }
}
