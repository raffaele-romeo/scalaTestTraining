import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters._

case class CustomKafkaProducer(brokerList: String) {

  val producerProps = {
    Map[String, Object](
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokerList,
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[
        StringSerializer].getName,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[
        StringSerializer].getName,
      ProducerConfig.ACKS_CONFIG -> "all",
      ProducerConfig.RETRIES_CONFIG -> "3",
      ProducerConfig.BATCH_SIZE_CONFIG -> "16384",
      ProducerConfig.LINGER_MS_CONFIG -> "1",
      ProducerConfig.BUFFER_MEMORY_CONFIG -> "33554432"
      //ProducerConfig.TRANSACTIONAL_ID_CONFIG -> "1",
      //ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG -> "true"
    ).asJava
  }

  val producer = new KafkaProducer[String, String](producerProps)

  def send(topic: String, key: String, value: String) {
    producer.send(new ProducerRecord[String, String](topic, key, value))
  }

}

object CustomKafkaProducer {
  var brokerList = ""
  lazy val instance = new CustomKafkaProducer(brokerList)
}

