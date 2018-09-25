name := "scalaTestTraining"

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.sky",
  scalaVersion := "2.11.12",
  test in assembly := {}
)

resolvers ++= Seq(
  "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Hortonworks" at "http://repo.hortonworks.com/content/repositories/releases/",
  "Hortonworks Groups" at "http://repo.hortonworks.com/content/groups/public/",
  "Apache Snapshots" at "https://repository.apache.org/content/repositories/releases/",
  "JBoss" at "https://repository.jboss.org"
)

fork in Test := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")
parallelExecution in Test := false
updateOptions := updateOptions.value.withCachedResolution(true)

//logLevel := Level.Error

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "test.jar",
    mainClass in assembly := Some("Main"),
    libraryDependencies ++= Seq(
      Libraries.sparkCore % "provided",
      Libraries.sparkSql % "provided",
      Libraries.cassandraConnectorUnshaded,
      Libraries.kafka,
      Libraries.avro,
      Libraries.kafkaAvroSerializer,

      //TEST
      Libraries.scalaTestEmbeddedKafka % "test",
      Libraries.cassandraConnectorEmbedded % "test",
      Libraries.cassandraAll % "test",
      Libraries.scalaTest % "test",
      Libraries.scalaCheck % "test",
      Libraries.sparkTestingBase % "test",
      Libraries.jacksonDatabind % "test",
      Libraries.jacksonCore % "test",
      Libraries.jacksonModuleScala % "test",
      Libraries.json4s % "test"
      //"com.google.guava" % "guava" % "16.0.1" % "test"
    ).map(_.exclude("org.slf4j", "log4j-over-slf4j")) // Excluded to allow Cassandra to run embedded
      .map(_.exclude("org.slf4j", "slf4j-log4j12")),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )

test in assembly := Seq(
  (test in Test).value
)