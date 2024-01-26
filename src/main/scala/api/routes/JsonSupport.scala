package api.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  case class OrderData(cart_value: Int, delivery_distance: Int, number_of_items: Int, time: String)
  implicit val OrderDataFormat: RootJsonFormat[OrderData] = jsonFormat4(OrderData)
}

object JsonSupport extends JsonSupport
