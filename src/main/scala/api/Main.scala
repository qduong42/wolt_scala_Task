package api

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.Http
import api.routes.{JsonSupport, MyApiRoutes}

import scala.concurrent.ExecutionContextExecutor

object Main extends App with JsonSupport {

  implicit val system: ActorSystem = ActorSystem("calculate-delivery-fee-api")
  implicit val materializer: Materializer = Materializer.createMaterializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val myApiRoutes = MyApiRoutes()

  val routes = myApiRoutes.route
  Http().newServerAt("localhost", 8080).bindFlow(routes)
  println(s"Server online at http://localhost:8080/")
}

