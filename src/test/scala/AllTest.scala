import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.{EmbeddedCassandra, SparkTemplate, YamlTransformations}
import com.holdenkarau.spark.testing.{HDFSCluster, RDDComparisons, SharedSparkContext}
import net.manub.embeddedkafka.EmbeddedKafka

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
      //session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {‘class’:’SimpleStrategy’, ‘replication_factor’:1};")
      session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class': 'SimpleStrategy', 'replication_factor':1};")
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

}
