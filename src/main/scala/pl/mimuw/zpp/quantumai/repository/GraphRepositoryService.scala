package pl.mimuw.zpp.quantumai.repository

import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.in
import pl.mimuw.zpp.quantumai.repository.dto.Graph
import zio.{ZIO, ZLayer}

trait GraphRepositoryService {
  def readGraphs(graphIds: Seq[String]): ZIO[Any, Throwable, Seq[Graph]]
}

case class GraphRepositoryServiceImpl(collection: MongoCollection[Graph]) extends GraphRepositoryService {
  def readGraphs(graphIds: Seq[String]): ZIO[Any, Throwable, Seq[Graph]] = {
    ZIO.fromFuture[Seq[Graph]] { _ =>
      collection.find(in("id", graphIds: _*)).toFuture()
    }
  }
}

object GraphRepositoryServiceImpl {
  val layer: ZLayer[MongoCollection[Graph], Nothing, GraphRepositoryService] = {
    ZLayer {
      for {
        mongoClient <- ZIO.service[MongoCollection[Graph]]
      } yield GraphRepositoryServiceImpl(mongoClient)
    }
  }
}
