//package api
//
//import akka.actor.ActorSystem
//import akka.http.javadsl.server.Directives.pathPrefix
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Route
//import akka.stream.{ActorMaterializer, Materializer}
//import api.routes.{JsonSupportMyApiRoutes
//
//import scala
//
//object Main with MyApiRoutes {
//
//  implicit val system: ActorSystem = ActorSystem("my-api")
//  implicit val materializer: Materializer = Materializer.createMaterializer(system)
//  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
//
//  val routes: Route = pathPrefix("v1") {
//    MyApiRoutes.route
//  }
//import scala.{App, concurrent}
//import scala.concurrent.ExecutionContextExecutor
//
//Http().newServerAt("localhost", 8080).bindFlow(routes)
//
//  println(s"Server online at http://localhost:8080/")
//}

//case class MyApiRoutes()

package api

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.Http
import api.routes.{JsonSupport, MyApiRoutes}

import scala.concurrent.ExecutionContextExecutor

object Main extends App with JsonSupport {

  implicit val system: ActorSystem = ActorSystem("my-api")
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val materializer: Materializer = Materializer.createMaterializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val myApiRoutes = MyApiRoutes()

  val routes = myApiRoutes.route
//  Http().bindAndHandle(routes, "0.0.0.0", 8080).foreach { binding =>
//    println(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
//  Http().bindAndHandle(routes, "localhost", 8080)
  Http().newServerAt("0.0.0.0", 8080).bindFlow(routes)
  println(s"Server online at http://localhost:8080/")
}

