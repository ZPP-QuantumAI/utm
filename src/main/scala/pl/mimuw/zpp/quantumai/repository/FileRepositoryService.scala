package pl.mimuw.zpp.quantumai.repository

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.{MongoCollection, ObservableFuture}
import org.mongodb.scala.gridfs.{GridFSBucket, GridFSFindObservable}
import org.mongodb.scala.model.Filters.equal
import pl.mimuw.zpp.quantumai.repository.dto.File
import zio.{ZIO, ZLayer}

import scala.concurrent.ExecutionContext.Implicits.global

trait FileRepositoryService {
  def readFile(solutionId: String): ZIO[Any, Throwable, File]
}

case class FileRepositoryServiceImpl(collection: GridFSBucket) extends FileRepositoryService {
  override def readFile(solutionId: String): ZIO[Any, Throwable, File] = {
    ZIO.fromFuture[File] { _ =>
      println(solutionId)
      collection.downloadToObservable(new ObjectId(solutionId)).head().map(bb => File(solutionId, bb))
    }
  }
}

object FileRepositoryServiceImpl {
  val layer: ZLayer[GridFSBucket, Nothing, FileRepositoryService] = {
    ZLayer {
      for {
        mongoClient <- ZIO.service[GridFSBucket]
      } yield FileRepositoryServiceImpl(mongoClient)
    }
  }
}
