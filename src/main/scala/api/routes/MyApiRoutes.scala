package api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

class MyApiRoutes extends Directives with JsonSupport {

    val route: Route =
      path("api" / "delivery-fee") {
        post {
          entity(as[OrderData]) { request =>
            println("Posted at http://localhost:8080/")
            if (isValidTime(request.time))
              complete("Request received successfully!")
            else
              complete(StatusCodes.BadRequest, "Invalid time format. Please provide the time in UTC ISO format.")
          }
        }
      } ~
        pathSingleSlash{
          get{
            println("Matching root path")
            complete("Hello this is the root endpoint!")
          }
        }
}

object MyApiRoutes {
  def apply() :MyApiRoutes = new MyApiRoutes
}