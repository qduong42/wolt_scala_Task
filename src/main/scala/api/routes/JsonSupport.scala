package api.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.format.DateTimeFormatter
import java.time.{DateTimeException, Instant, ZoneId}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  case class OrderData(cart_value: Int, delivery_distance: Int, number_of_items: Int, time: String)
  case class DeliveryFeeResponse(delivery_fee: Int)
  implicit val OrderDataFormat: RootJsonFormat[OrderData] = jsonFormat4(OrderData)
  implicit val deliveryFeeResponseFormat: RootJsonFormat[DeliveryFeeResponse] = jsonFormat1(DeliveryFeeResponse)

  def createDeliveryFeeResponse(deliveryFeeInCents: Int): DeliveryFeeResponse = {
    // Marshal the case class instance into JSON using JsonSupport
    DeliveryFeeResponse(deliveryFeeInCents)
  }

  def isValidTime(time: String): Boolean = {
    val formatter = DateTimeFormatter.ISO_INSTANT

    try {
      Instant.from(formatter.parse(time))
      true
    } catch {
      case _: DateTimeException =>
        false
    }
  }
}

object JsonSupport extends JsonSupport
