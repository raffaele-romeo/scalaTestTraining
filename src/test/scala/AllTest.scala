import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import com.holdenkarau.spark.testing.HDFSCluster
import kafka.server.KafkaConfig
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.types.{StructField, StructType, _}

import scala.collection.JavaConverters._


class AllTest extends UnitSpec with EmbeddedKafka
  with SparkTemplate with EmbeddedCassandra {

  override def clearCache(): Unit = CassandraConnector.evictCache()

  //Sets up CassandraConfig and SparkContext
  useCassandraConfig(Seq(YamlTransformations.Default))
  useSparkConf(defaultConf)
  val connector = CassandraConnector(defaultConf)

  var hdfsCluster: HDFSCluster = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    EmbeddedKafka.start()

    connector.withSessionDo { session =>
      session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};")
      session.execute("CREATE TABLE test.long_tweets(id bigint, username text, text text, PRIMARY KEY(id, username));")
    }

    hdfsCluster = new HDFSCluster
    hdfsCluster.startHDFS()
  }

  override def afterAll(): Unit = {
    try {
      hdfsCluster.shutdownHDFS()
      EmbeddedKafka.stop()
    } catch {
      case e: Throwable => None
    }
    super.afterAll()
  }

  implicit val spark: SparkSession = sparkSession
  import spark.implicits._

  // define a schema for the data in the tweets CSV
  val tweetsSchema = StructType(Array(
    StructField("id", LongType, true),
    StructField("username", StringType, true),
    StructField("text", StringType, true)))

  val parquetFileName = "/tweets.parquet"
  val numberMessages = 50
  val messages = for (i <- 1 to numberMessages) yield genRandomTweet


  "Write parquet" should "write file parquet on HDFS" in {

    val hdfsPath = hdfsCluster.getNameNodeURI() + parquetFileName

    implicit val econder = Encoders.STRING
    val messagesDataset: Dataset[Tweet] = spark.createDataset(messages)

    /*
    val lines = sparkSession.read.option("header", "false").schema(tweetsSchema)
      .csv("./src/test/resources/tweets.csv").as[Tweet]
      */

    messagesDataset.write.parquet(hdfsPath)

    val path = new Path(hdfsPath)
    val fs = FileSystem.get(path.toUri, new Configuration())

    assert( fs.exists(path) )
  }

  "Write dataset to cassandra" should "write all record" in{
    val hdfsPath = hdfsCluster.getNameNodeURI() + parquetFileName

    sparkSession.read.option("header", "false").schema(tweetsSchema).parquet(hdfsPath).as[Tweet]
      .write.cassandraFormat("long_tweets", "test").save()

    assert(spark.read.cassandraFormat("long_tweets", "test").load().as[Tweet].count() == numberMessages)
  }

  "Write Dataset to kafka" should "write all record" in {

    implicit val config: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      customBrokerProperties = Map(
        KafkaConfig.LogCleanerDedupeBufferSizeProp -> 2000000.toString
      ),
      customConsumerProperties = Map(
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> 16384.toString,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false"
      )
    )
    implicit val serializer = new StringSerializer()
    implicit val deserializer = new StringDeserializer()


    val consumerPollTimeout = 5000
    val hdfsPath = s"${hdfsCluster.getNameNodeURI()}/tweets.parquet"
    val topic = "test"

    val readTable = spark.read.cassandraFormat("long_tweets", "test").load().as[Tweet]

    readTable.foreachPartition(iter => {
      CustomKafkaProducer.brokerList = s"localhost:${config.kafkaPort}"
      val producer = CustomKafkaProducer.instance
      iter.foreach(tweet => {
        producer.send(topic, tweet.id + " " + tweet.username, tweet.text)
      })
    })

    val consumer = kafkaConsumer
    consumer.subscribe(List(topic).asJava)

    val records = consumer
      .poll(consumerPollTimeout)
      .iterator()

    assert(records.hasNext)

    while (records.hasNext) {
      val record = records.next()
      println(s"key: ${record.key()}, value: ${record.value()}")
    }

  }
}
