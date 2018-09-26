import org.scalatest._
import org.scalatest.prop.Checkers

abstract class UnitSpec extends FlatSpec
  with OptionValues with Inside with Inspectors with Checkers with Matchers
  with BeforeAndAfterAll with UtilityMethodForTest