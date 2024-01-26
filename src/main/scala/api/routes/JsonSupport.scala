package api.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.format.DateTimeFormatter
import java.time.{DateTimeException, Instant, ZoneId}


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  case class OrderData(cart_value: Int, delivery_distance: Int, number_of_items: Int, time: String)
  implicit val OrderDataFormat: RootJsonFormat[OrderData] = jsonFormat4(OrderData)
//  sealed trait TimeValidationResult
//  private case class ValidTime(instant: Instant) extends TimeValidationResult
//  private case class InvalidTime(errorMessage: String) extends TimeValidationResult

  def isValidTime(time: String): Boolean = {
    val formatter = DateTimeFormatter.ISO_INSTANT

    try {
      val parsedTime = Instant.from(formatter.parse(time))
      true
    } catch {
      case _: DateTimeException =>
        false
    }
  }
}

object JsonSupport extends JsonSupport
