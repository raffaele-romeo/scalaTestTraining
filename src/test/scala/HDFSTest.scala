
import com.holdenkarau.spark.testing.{HDFSCluster, RDDComparisons, SharedSparkContext}
import org.scalatest.FunSuite

class HDFSTest extends UnitSpec with SharedSparkContext  with RDDComparisons {

  override implicit def reuseContextIfPossible: Boolean = true

  var hdfsCluster: HDFSCluster = _
  override def beforeAll(): Unit = {
    super.beforeAll()
    hdfsCluster = new HDFSCluster
    hdfsCluster.startHDFS()
  }

  override def afterAll() {
    try {
      hdfsCluster.shutdownHDFS()
    }catch {
      case e: Throwable => None
    }
    super.afterAll()
  }

  "Name node URI" should "be hdfs://localhost:8020" in {
    val nameNodeURI = hdfsCluster.getNameNodeURI()
    assert(nameNodeURI == "hdfs://localhost:8020")
  }
}