import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import com.holdenkarau.spark.testing.{HDFSCluster, RDDComparisons, SharedSparkContext}
import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.cassandra._
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
    }catch {
      case e: Throwable => None
    }
    super.afterAll()
  }

  "All test method" should "work" in {

    implicit val serializer = new StringSerializer()
    implicit val deserializer = new StringDeserializer()
    implicit val spark: SparkSession = sparkSession
    import spark.implicits._

    // define a schema for the data in the tweets CSV
    val tweetsSchema = StructType(Array(
      StructField("id", LongType, true),
      StructField("username", StringType, true),
      StructField("text", StringType, true)))

    val consumerPollTimeout = 5000
    val hdfsPath = s"${hdfsCluster.getNameNodeURI()}/tweets.parquet"
    val topic = "test"

    // read the csv
    val lines = sparkSession.read.option("header", "false").schema(tweetsSchema)
      .csv("./src/test/resources/tweets.csv").as[Tweet]
    lines.show(false)
    //write on hdfs
    lines.write.parquet(hdfsPath)
    //read parquet
    val linesFromParquet = sparkSession.read.option("header", "false").schema(tweetsSchema).parquet(hdfsPath).as[Tweet]
    linesFromParquet.show(false)
    assert(linesFromParquet.count() == 4)

    linesFromParquet.write.cassandraFormat("long_tweets", "test").save()

    val linesFromCassandra = spark.read.cassandraFormat("long_tweets", "test").load().as[Tweet]

    assert(linesFromCassandra.count() == 4)

    linesFromCassandra.show(false)
    val messages = linesFromCassandra.map(tweet => (tweet.id + " " + tweet.username, tweet.text))
      .collect().toList

    publishToKafka(topic, messages)

    val consumer = kafkaConsumer
    consumer.subscribe(List(topic).asJava)

    val records = consumer
      .poll(consumerPollTimeout)
      .iterator()

    while (records.hasNext) {
      val record = records.next()
      println(s"key: ${record.key()}, value: ${record.value()}")
    }

  }
}
