import kafka.admin.AdminUtils
import kafka.server.KafkaConfig
import kafka.utils.ZkUtils

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.collection.JavaConverters._

class KafkaTest extends UnitSpec with EmbeddedKafka {

  val consumerPollTimeout: Long = 5000
  val numberMessages = 5000

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()
  }

  override def afterAll(): Unit = {
    EmbeddedKafka.stop()
    super.afterAll()
  }

  "The publishToKafka method" should "publish synchronously a String message and key to Kafka" in {
    implicit val serializer: StringSerializer = new StringSerializer()
    implicit val deserializer: StringDeserializer = new StringDeserializer()
    implicit val config: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      customBrokerProperties = Map(
        KafkaConfig.LogCleanerDedupeBufferSizeProp -> 2000000.toString
      ),
      customConsumerProperties = Map(
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> 16384.toString,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false"
      )
    )

    val messages = for (i <- 1 to numberMessages)
      yield trasformObjectToOriginalJson(genRandomMessage).split('|')
      match { case Array (x, y) => (x, y) }

    val topic = "eu-portability"

    createCustomTopic(
      topic,
      Map(
        TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_COMPACT),
      partitions = 16)

    val zkSessionTimeoutMs = 10000
    val zkConnectionTimeoutMs = 10000
    val zkSecurityEnabled = false

    val zkUtils = ZkUtils(s"localhost:${config.zooKeeperPort}",
      zkSessionTimeoutMs,
      zkConnectionTimeoutMs,
      zkSecurityEnabled)

    try {
      AdminUtils
        .topicExists(zkUtils, topic) shouldBe true

      zkUtils
        .getTopicPartitionCount(topic)
        .value shouldBe 16
    } finally zkUtils.close()


    val producer = new KafkaProducer[String, String](
      Map[String, Object](
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> s"localhost:${config.kafkaPort}",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[
          StringSerializer].getName,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[
          StringSerializer].getName,
        ProducerConfig.ACKS_CONFIG -> "all",
        ProducerConfig.RETRIES_CONFIG -> "3",
        ProducerConfig.BATCH_SIZE_CONFIG -> "16384",
        ProducerConfig.LINGER_MS_CONFIG -> "1",
        ProducerConfig.BUFFER_MEMORY_CONFIG -> "33554432",
        ProducerConfig.TRANSACTIONAL_ID_CONFIG -> "1",
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG -> "true"
      ).asJava)


    messages.size shouldBe numberMessages

    producer.initTransactions()

    try {
      producer.beginTransaction()
      messages.toList.foreach { message =>
        producer.send(new ProducerRecord[String, String](topic, message._1, message._2))
      }
      producer.commitTransaction()
    }catch {
      case e: KafkaException => producer.abortTransaction()
    }

    val consumer = kafkaConsumer
    consumer.subscribe(List(topic).asJava)

    val records = consumer.poll(consumerPollTimeout)

    records.iterator().hasNext shouldBe true
    val record = records.iterator().next()

    records.count() shouldBe numberMessages

    //record.key() shouldBe key
    //record.value() shouldBe message

    consumer.commitSync()
    consumer.close()
  }

  it should "avro message " in {

    /*
    import avro._

    val message = TestAvroClass("name")
*/
  }
}
