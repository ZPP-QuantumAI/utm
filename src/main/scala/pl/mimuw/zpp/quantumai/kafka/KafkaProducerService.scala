//import domain.GradeResponse
//import zio.kafka.producer.{Producer, ProducerSettings}
//import zio._
//
//trait KafkaProducerService {
//  def produce(gradeResponse: GradeResponse): ZIO[Any, Throwable, Unit]
//}
//
//case class KafkaProducerServiceImpl(producer: Producer) extends KafkaProducerService {
//  override def produce(gradeResponse: GradeResponse): ZIO[Any, Throwable, Unit] = {
//    // Implement logic to produce messages to Kafka
//    ???
//  }
//}
//
//object KafkaProducerService {
//  val producerLayer: ZLayer[Any, Throwable, KafkaProducerService] =
//    ZLayer {
//      for {
//        producer <- Producer.make(ProducerSettings(List("localhost:9092")))
//      } yield KafkaProducerServiceImpl(producer)
//    }
//}