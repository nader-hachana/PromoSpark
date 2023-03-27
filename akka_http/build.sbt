lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.17",
    organization := "com.cognira.akka",
    name := "akka",
    sbtVersion := "1.2.8"
  )
  
assemblyJarName in assembly := s"akka_2.12-0.1.jar"

logLevel in assembly := Level.Error

libraryDependencies ++= Seq(
      "com.datastax.oss" % "java-driver-core" % "4.9.0",
      "com.typesafe.akka" %% "akka-http" % "10.2.0",
      "com.typesafe.akka" %% "akka-stream" % "2.6.9",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.9",
      "com.typesafe.play" %% "play-json" % "2.9.0"
    )

assemblyMergeStrategy in assembly := {
  case "application.conf" => MergeStrategy.concat
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}