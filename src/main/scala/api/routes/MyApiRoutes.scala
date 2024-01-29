package api.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import api.services.DeliveryFeeCalculationService

class MyApiRoutes extends Directives with JsonSupport with DeliveryFeeCalculationService {

  val route: Route =
    path("api" / "calculate-delivery-fee") {
      post {
        extractRequest { request =>
          if (request.entity.contentType == ContentTypes.`application/json`) {
            entity(as[api.routes.JsonSupport.OrderData]) { request =>
              if (isValidTime(request.time)) {
                val deliveryFeeInCents = calculateDeliveryFee(request)
                val marshalledJson = createDeliveryFeeResponse(deliveryFeeInCents)
                complete(StatusCodes.OK, marshalledJson)
              } else
                complete(StatusCodes.BadRequest, "Invalid time format. Please provide the time in UTC ISO format.")
            }
          }
          else
            complete(StatusCodes.UnsupportedMediaType, "Unsupported media type. Please use Content-Type: application/json.")
        }
      } ~
        pathSingleSlash {
          get {
            println("Matching root path")
            complete("Hello this is the root endpoint!")
          }
        }
    }
}
object MyApiRoutes {
  def apply() :MyApiRoutes = new MyApiRoutes
}