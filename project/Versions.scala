import sbt._

import scala.languageFeature._

object Versions {

  lazy val spark = "2.2.0"
  lazy val sparkTesting = spark + "_0.8.0"
  lazy val scala = "2.11.12"
  lazy val sparkCassandraConnector = "2.0.3"
  lazy val cassandra = "3.0.0"
  lazy val kafka = "1.0.0"
  lazy val scalaTest = "3.0.5"
  lazy val scalaCheck = "1.14.0"
  lazy val confluent = "4.0.0"
  lazy val avro = "1.8.2"
  lazy val json4s = "3.2.11"
  lazy val akkaPersistenceCassandra = "0.89"
  lazy val jackson = "2.9.1"
  lazy val guava = "16.0.1"
}

object Libraries {
  //Spark dependencies
  lazy val sparkCore = "org.apache.spark" %% "spark-core" % Versions.spark
  lazy val sparkHive = "org.apache.spark" %% "spark-hive" % Versions.spark
  lazy val sparkTestingBase = "com.holdenkarau" %% "spark-testing-base" % Versions.sparkTesting
  lazy val sparkSql = "org.apache.spark" %% "spark-sql" % Versions.spark

  //Cassandra dependencies
  lazy val cassandraConnectorUnshaded = "com.datastax.spark" %% "spark-cassandra-connector-unshaded" % Versions.sparkCassandraConnector
  lazy val cassandraConnectorEmbedded = "com.datastax.spark" %% "spark-cassandra-connector-embedded" % Versions.sparkCassandraConnector
  lazy val cassandraAll = "org.apache.cassandra" % "cassandra-all" % Versions.cassandra
  lazy val cassandraConnector = "com.datastax.spark" %% "spark-cassandra-connector" % Versions.sparkCassandraConnector
  lazy val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Versions.akkaPersistenceCassandra

  //Kafka dependencies
  lazy val kafka = "org.apache.kafka" %% "kafka" % Versions.kafka
  lazy val kafkaStreams = "org.apache.kafka" % "kafka-streams" % Versions.kafka
  lazy val kafkaAvroSerializer = "io.confluent" % "kafka-avro-serializer" % Versions.confluent
  lazy val kafkaSchemaRegistry = "io.confluent" % "kafka-schema-registry" % Versions.confluent
  lazy val scalaTestEmbeddedKafka = "net.manub" %% "scalatest-embedded-kafka" % Versions.kafka
  lazy val scalaTestEmbeddedKafkaStreams =  "net.manub" %% "scalatest-embedded-kafka-streams" % Versions.kafka

  //Scala dependencies
  lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
  lazy val scalatic = "org.scalactic" %% "scalactic" % Versions.scalaTest
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck

  //Json4s&Jackson dependencies
  lazy val json4s = "org.json4s" %% "json4s-native" % Versions.json4s
  lazy val json4sJakson = "org.json4s" %% "json4s-jackson" % Versions.json4s
  lazy val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson
  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % Versions.jackson
  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson

  //Avro dependencies
  lazy val avro = "org.apache.avro" % "avro" % Versions.avro

  //Guava dependencies
  lazy val guava = "com.google.guava" % "guava" % Versions.guava
}