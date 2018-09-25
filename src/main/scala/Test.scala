import scala.io.Source

object Test {
  def main(args: Array[String]): Unit = {
    val filename = "C:\\Users\\RomeoRa\\workspacesSKY\\scalaTestTraining\\src\\test\\resources\\cassandra-3.2.yaml.template"
    val filename2 = "cassandra-3.2.yaml.template"
    for (line <- Source.fromFile(filename).getLines) {
      //println(line)
    }

    //ClassLoader.getSystemResourceAsStream(filename2).available()
    ClassLoader.getSystemResourceAsStream("cassandra-3.2.yaml.template")

    /*
    val x = getClass.getResourceAsStream("/cassandra-3.2.yaml.template")
    println(x.read())
    val b = ClassLoader.getSystemResource("cassandra-3.2.yaml.template")
    println(b)

    val a = ClassLoader.getSystemResourceAsStream("cassandra-3.2.yaml.template")
    println(a.read())
    */
  }
}
