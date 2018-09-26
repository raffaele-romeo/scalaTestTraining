import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import org.apache.spark.sql.cassandra.{DataFrameWriterWrapper, _}
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}

class CassandraTest extends UnitSpec with SparkTemplate with EmbeddedCassandra {
  override def clearCache(): Unit = CassandraConnector.evictCache()

  //Sets up CassandraConfig and SparkContext
  useCassandraConfig(Seq(YamlTransformations.Default))
  useSparkConf(defaultConf)
  val connector = CassandraConnector(defaultConf)

  val numberMessages = 50

  override def beforeAll(): Unit = {
    super.beforeAll()
    connector.withSessionDo { session =>
      //session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {‘class’:’SimpleStrategy’, ‘replication_factor’:1};")
      session.execute("CREATE KEYSPACE euportability WITH replication = {'class':'SimpleStrategy','replication_factor':1};")
      session.execute("CREATE TABLE euportability.activity (proposition text, profile_id text, user_type text, household_id text, provider text, provider_territory text, country_code text, activity_timestamp text, PRIMARY KEY ((profile_id, country_code, provider)))")
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  "Write RDD to cassandra" should "publish push records in Cassandra table" in {
    implicit val spark: SparkSession = sparkSession
    import spark.implicits._

    val keyspace = "euportability"
    val table = "activity"

    val messages = for (i <- 1 to numberMessages) yield genRandomMessage

    implicit val econder = Encoders.STRING
    val messagesDataset: Dataset[Message] = spark.createDataset(messages).dropDuplicates(List("profile_id", "country_code", "provider"))

    messagesDataset.write.cassandraFormat(table, keyspace).save()

    val datasetReadCount = spark.read.cassandraFormat(table, keyspace).load().as[Message].count()

    assert(datasetReadCount == messagesDataset.count())

    val newMessages = (for (i <- 1 to numberMessages) yield genRandomMessage).++:(messages)

    val datasetToWrite = spark.createDataset(newMessages).rdd
      .repartitionByCassandraReplica(keyspace, table)
      .leftJoinWithCassandraTable[Message](keyspace, table)
      .filter(_._2.isEmpty)
      .map(_._1)

    val countJoin = datasetToWrite.toDS().dropDuplicates(List("profile_id", "country_code", "provider")).count()

    datasetToWrite.saveToCassandra(keyspace, table)

    val datasetRead2 = spark.read.cassandraFormat(table, keyspace).load().as[Message]

    assert(datasetRead2.count() == countJoin + datasetReadCount )

  }
}
