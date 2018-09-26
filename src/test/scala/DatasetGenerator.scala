/*
import com.holdenkarau.spark.testing.{DatasetGenerator, SharedSparkContext}
import org.apache.spark.sql.{Dataset, SQLContext}
import org.scalacheck.{Arbitrary, Gen}


object DatasetGenerator extends UnitSpec with SharedSparkContext{

  val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._

  val tweetGen: Gen[Dataset[Seq[Tweet]]] = DatasetGenerator.arbitraryDataset()

}
  val tweetGen: Gen[Dataset[Seq[Tweet]]] =
  DatasetGenerator.genSizedDataset[Seq[Car]](sqlContext) { size =>
    val slowCarsTopNumber = math.ceil(size * 0.1).toInt
    def carGenerator(speed: Gen[Int]): Gen[Car] = for {
      name <- Arbitrary.arbitrary[String]
      speed <- speed
    } yield Car(name, speed)

    val cars: Gen[List[Car]] = for {
      slowCarsNumber: Int <- Gen.choose(0, slowCarsTopNumber)
      slowCars: List[Car] <- Gen.listOfN(slowCarsNumber, carGenerator(Gen.choose(0, 20)))
      normalSpeedCars: List[Car] <- Gen.listOfN(
        size - slowCarsNumber,
        carGenerator(Gen.choose(21, 150))
      )
    } yield {
      slowCars ++ normalSpeedCars
    }
    cars

}
*/

