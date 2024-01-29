package api.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import api.services.DeliveryFeeCalculationService

class MyApiRoutes extends Directives with JsonSupport with DeliveryFeeCalculationService {

  val route: Route =
    pathPrefix("api") {
      path("calculate-delivery-fee") {
        post {
          handleCalculateDeliveryFee
        }
      } ~
        handleRootRequests("/api/")
    }~
      handleRootRequests("/")

  private def handleCalculateDeliveryFee: Route = {
    extractRequest { request =>
      if (request.entity.contentType == ContentTypes.`application/json`) {
        entity(as[api.routes.JsonSupport.OrderData]) { requestData =>
          handleDeliveryFeeRequest(requestData)
        }
      } else {
        complete(StatusCodes.UnsupportedMediaType, "Unsupported media type. Please use Content-Type: application/json.")
      }
    }
  }

  private def handleDeliveryFeeRequest(requestData: JsonSupport.OrderData): Route = {
    if (isValidTime(requestData.time) && !isOneFieldNegative(requestData)) {
      val deliveryFeeInCents = calculateDeliveryFee(requestData)
      complete(StatusCodes.OK, createDeliveryFeeResponse(deliveryFeeInCents))
    } else if (isOneFieldNegative(requestData)) {
      complete(StatusCodes.BadRequest, "One or more input field values except time is negative")
    } else {
      complete(StatusCodes.BadRequest, "Invalid time format. Please provide a valid date and time in ISO format.")
    }
  }
  private def handleRootRequests(message :String): Route = {
    pathEndOrSingleSlash {
      complete(StatusCodes.NotFound, s"The requested resource $message could not be found.")
    }
  }
}

object MyApiRoutes {
  def apply(): MyApiRoutes = new MyApiRoutes
}
