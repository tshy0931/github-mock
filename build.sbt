name := "github-mock"

version := "0.1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.4",
  "com.typesafe.akka" % "akka-slf4j_2.12" % "2.5.1",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.1",
  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.1",
  "com.outworkers" % "phantom-dsl_2.12" % "2.9.2",
  "joda-time" % "joda-time" % "2.9.9"
)
