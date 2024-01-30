package api.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import java.time.format.{DateTimeFormatter, ResolverStyle}
import java.time.{DateTimeException, Instant}

final case class OrderData(cart_value: Int, delivery_distance: Int, number_of_items: Int, time: String)
final case class DeliveryFeeResponse(delivery_fee: Int)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val OrderDataFormat: RootJsonFormat[OrderData] = jsonFormat4(OrderData)
  implicit val deliveryFeeResponseFormat: RootJsonFormat[DeliveryFeeResponse] = jsonFormat1(DeliveryFeeResponse)

  def createDeliveryFeeResponse(deliveryFeeInCents: Int): DeliveryFeeResponse = {
    DeliveryFeeResponse(deliveryFeeInCents)
  }

  def isValidTime(time: String): Boolean = {
    val formatter = DateTimeFormatter.ISO_INSTANT.withResolverStyle(ResolverStyle.STRICT)
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
