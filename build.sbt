import sbt.project
ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "WoltScalaTask"
  )
resolvers += "Akka library repository".at("https://repo.akka.io/maven")

val AkkaVersion = "2.9.0"
val AkkaHttpVersion = "10.6.0"
val AkkaStreamVersion = "2.9.0"
val ScalaTestVersion = "3.2.15"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
)