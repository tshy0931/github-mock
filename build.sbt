lazy val root = (project in file("."))
  .settings(
    name := "github-mock",
 //   scalaVersion := "2.11.8",
    version := "0.1.0-SNAPSHOT"
  )

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.4",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.17"
)
