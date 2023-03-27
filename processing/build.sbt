lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.17",
    organization := "com.cognira.Challenge",
    name := "challenge",
    sbtVersion := "1.2.8"
  )
  
assemblyJarName in assembly := s"challenge_2.12-0.1.jar"

logLevel in assembly := Level.Error

libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.2.1" % "provided",
      "org.apache.spark" %% "spark-sql" % "3.2.1" % "provided",
      "com.datastax.spark" %% "spark-cassandra-connector-assembly" % "3.2.0",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.0"
    )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}