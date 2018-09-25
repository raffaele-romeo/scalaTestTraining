import org.scalatest._

abstract class UnitSpec extends FlatSpec
  with OptionValues with Inside with Inspectors with UtilityMethodForTest
  with Matchers with BeforeAndAfterAll