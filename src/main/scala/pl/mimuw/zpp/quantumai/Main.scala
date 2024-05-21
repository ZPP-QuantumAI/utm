package pl.mimuw.zpp.quantumai

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.gridfs.GridFSBucket
import pl.mimuw.zpp.quantumai.grade.GradingServiceImpl
import pl.mimuw.zpp.quantumai.kafka._
import pl.mimuw.zpp.quantumai.repository.dto.{File, Graph, Node}
import pl.mimuw.zpp.quantumai.repository.{FileRepositoryServiceImpl, GraphRepositoryServiceImpl}
import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.kafka.producer.{Producer, ProducerSettings}

import scala.concurrent.Await
import scala.sys.env

object Main extends ZIOAppDefault {
  private val kafkaConsumer =
    Consumer.make(ConsumerSettings(List("localhost:9092", "kafka-service:9092")).withGroupId("utm"))
  private val kafkaProducer = Producer.make(ProducerSettings(List("localhost:9092", "kafka-service:9092")))

  val username = env.getOrElse("MONGO_USERNAME", "")
  val password = env.getOrElse("MONGO_PASSWORD", "")
  private val mongoClient       = MongoClient(s"mongodb://${username}:${password}@57.129.21.96:27017")
  private val fileCodecRegistry = fromRegistries(fromProviders(classOf[File]), DEFAULT_CODEC_REGISTRY)
  private val graphCodecRegistry =
    fromRegistries(fromProviders(classOf[Graph], classOf[Node]), DEFAULT_CODEC_REGISTRY)
  private val filesCollection = GridFSBucket(mongoClient.getDatabase("test"))
  private val graphCollection = mongoClient
    .getDatabase("test")
    .withCodecRegistry(graphCodecRegistry)
    .getCollection[Graph]("euclideanGraph")

  override def run: ZIO[Any, Throwable, Unit] =
    KafkaConsumerService
      .consume()
      .provide(
        GradingServiceImpl.layer,
        FileRepositoryServiceImpl.layer,
        GraphRepositoryServiceImpl.layer,
        KafkaConsumerServiceImpl.layer,
        KafkaProducerServiceImpl.layer,
        ZLayer.scoped(ZIO.succeed(filesCollection)),
        ZLayer.scoped(ZIO.succeed(graphCollection)),
        ZLayer.scoped(kafkaConsumer),
        ZLayer.scoped(kafkaProducer)
      )
}
