import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import org.apache.spark.sql.cassandra.DataFrameWriterWrapper
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}

class CassandraTest extends UnitSpec with SparkTemplate with EmbeddedCassandra {
  override def clearCache(): Unit = CassandraConnector.evictCache()

  //Sets up CassandraConfig and SparkContext
  useCassandraConfig(Seq(YamlTransformations.Default))
  useSparkConf(defaultConf)
  val connector = CassandraConnector(defaultConf)

  val numberMessages = 5

  override def beforeAll(): Unit = {
    super.beforeAll()
    connector.withSessionDo { session =>
      //session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {‘class’:’SimpleStrategy’, ‘replication_factor’:1};")
      session.execute("CREATE KEYSPACE IF NOT EXISTS euportability WITH replication = {'class': 'SimpleStrategy', 'replication_factor':1};")
      session.execute("CREATE TABLE euportability.activity (  proposition text, profileId text, userType text, householdId text, provider text, providerTerritory text, countryCode text, activityTimestamp text, PRIMARY KEY (profileId))")
    }
  }

  "Write RDD to cassandra" should "publish push records in Cassandra table" in {
    implicit val spark: SparkSession = sparkSession
    import spark.implicits._

    val messages = for (i <- 1 to numberMessages) yield genRandomMessage

    implicit val econder = Encoders.STRING
    val messagesDataset: Dataset[Message] = spark.createDataset(messages)



    messagesDataset.write.cassandraFormat("activity", "euportability").save()
  }
}
