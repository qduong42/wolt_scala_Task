//import sbt.Keys.resolvers
//import sbt.project
//ThisBuild / version := "1.0"
//
//ThisBuild / scalaVersion := "2.13.12"
//
//lazy val root = (project in file("."))
//  .settings(
//    name := "WoltScalaTask"
//      resolvers += "Akka library repository".at("https://repo.akka.io/maven")
//
//val AkkaVersion = "10.5.0"
//val AkkaHttpVersion = "10.6.0"
//val AkkaStreamVersion = "2.9.0"
//val ScalaTestVersion = "3.2.15"
//  .settings(libraryDependencies ++= Seq(
//  "com.typesafe.akka" %% "akka-http-testkit" % AkkaVersion % Test,
//  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
//  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
//  "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
//  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
//  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
//))


ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.13.12"

val AkkaHttpVersion = "10.5.3"
val AkkaStreamVersion = "2.8.5"
val ScalaTestVersion = "3.2.15"

lazy val root = (project in file("."))
  .settings(
    name := "WoltScalaTask",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven")
  )
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaStreamVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-testkit" % AkkaStreamVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaStreamVersion % Test,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
    "ch.qos.logback" % "logback-classic" % "1.4.12"
  ))
