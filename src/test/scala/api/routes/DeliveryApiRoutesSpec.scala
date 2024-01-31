package api.routes

import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ContentTypes, HttpHeader, HttpMethods, StatusCodes}
import akka.http.scaladsl.server.{MalformedRequestContentRejection, MethodRejection}
import akka.http.scaladsl.testkit._
import api.support.{DeliveryFeeResponse, JsonSupport}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeliveryApiRoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  val DeliveryApiRoutes = new DeliveryApiRoutes

  "HTTP Request to /api/calculate-calculate-delivery-fee" should {
    "be rejected for other requests than POST request" when {
      "GET request" in{
        Get("/api/calculate-delivery-fee") ~> DeliveryApiRoutes.route ~> check {
          rejection shouldEqual MethodRejection(HttpMethods.POST)
        }
      }
      "DELETE request" in{
        Delete("/api/calculate-delivery-fee") ~> DeliveryApiRoutes.route ~> check {
          rejection shouldEqual MethodRejection(HttpMethods.POST)
        }
      }
    }
    "return a success message for POST request" in {
      val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
    "return a response in application/json for POST request" in {
      val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {

        val contentTypeHeader: Option[HttpHeader] = header[`Content-Type`]

        contentTypeHeader.exists(_.value == ContentTypes.`application/json`.value) shouldEqual true

      }
    }
    "return a bad request for POST requests with invalid time in body" when {
      "random string input not remotely related to time" in {
        val invalidRequestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide a valid date and time in ISO format."
        }
      }
      "time is almost like in ISO but missing T and Z" in {
        val invalidRequestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-1513:00:00"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide a valid date and time in ISO format."
        }
      }
      "time given with invalid Date of month" in {
        val invalidRequestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2022-01-35T10:30:00Z"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide a valid date and time in ISO format."
        }
      }
    }
    "return a bad request:malformed for POST requests missing required members in body" when {
      "cart_value is missing in POST request body" in {
        val invalidRequestEntity = """{"delivery_distance": 1000, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'cart_value'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "delivery_distance is missing in POST request body" in {
        val invalidRequestEntity = """{"cart_value": 1000, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'delivery_distance'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "number_of_items is missing in POST request body" in {
        val invalidRequestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "time": "invalid-time"}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'number_of_items'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "time is missing in POST request body" in {
        val invalidRequestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4}"""

        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'time'", _: spray.json.DeserializationException) =>
          }
        }
      }
    }
    "return a bad request:malformed for POST requests with wrong types for fields in body" in {
      val invalidRequestEntity = """{"cart_value": "1000", "delivery_distance": 1000, "number_of_items": 4, "time": "invalid-time"}"""

      Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
        rejection should matchPattern {
          case MalformedRequestContentRejection("Expected Int as JsNumber, but got \"1000\"", _: spray.json.DeserializationException) =>
        }
      }
    }
    "return a bad request \"One or more input field values except time is negative\"" in {
      val invalidRequestEntity = """{"cart_value": -500, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> DeliveryApiRoutes.route ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "One or more input field values except time is negative"
      }
    }
    "Unsupported Media Type response for POST requests with headers other than JSON for example text/plain" in {
      val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`text/plain(UTF-8)`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
        status shouldEqual StatusCodes.UnsupportedMediaType
      }
    }
    "return correct delivery distance fee calculation" when {
      "delivery distance is 0 returns delivery fee first 1000m" in {
        val deliveryDistance = 0
        DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 200
      }
      "delivery distance is 1 - 500" in {
        val deliveryDistance = 499
        DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 200
      }
      "delivery distance is 1000" in {
        val deliveryDistance = 1000
        DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 200
      }
      "delivery distance is 1500" in {
        val deliveryDistance = 1500
        DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 300
      }
      "delivery distance is 1999" in {
        val deliveryDistance = 1999
        DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 400
      }
      "delivery distance is 2000" in {
          val deliveryDistance = 2000
          DeliveryApiRoutes.deliveryDistanceFee(deliveryDistance) equals 400
      }
    }
    "return correct cart value fee calculation" when {
      "cart value below MinimumCartValueNoSurcharge" in {
        val cartValue = 245
        DeliveryApiRoutes.cartValueCheck(cartValue) equals 755
      }
      "cart value equals MinimumCartValueNoSurcharge" in {
        val cartValue = 1000
        DeliveryApiRoutes.cartValueCheck(cartValue) equals 0
      }
      "cart value more than MinimumCartValueNoSurcharge" in {
        val cartValue = 1500
        DeliveryApiRoutes.cartValueCheck(cartValue) equals 0
      }
      "cart value equals CartValueNeededForFreeDelivery" in {
        val requestEntity = """{"cart_value": 20000, "delivery_distance": 5000, "number_of_items": 9, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 0
        }
      }
    }
    "return correct number of items fee surcharge calculation" when {
      "items equals MaximumNumberOfItemsNoSurcharge : 4" in {
        val noOfItems = 4
        DeliveryApiRoutes.cartValueCheck(noOfItems) equals 0
      }
      "items more than MaximumNumberOfItemsNoSurcharge but equal to MaximumNumberOfItemsNoBulkSurcharge" in {
        val noOfItems = 12
        DeliveryApiRoutes.cartValueCheck(noOfItems) equals 400
      }
      "items more than both MaximumNumberOfItemsNoSurcharge and MaximumNumberOfItemsNoBulkSurcharge" in {
        val noOfItems = 14
        DeliveryApiRoutes.cartValueCheck(noOfItems) equals 620
      }
    }
    "delivery cost subject to 1.2x multiplier to fee influenced by delivery time" when {
      "delivery time is on Friday between 15:00 and 19:00 inclusive" in {
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-19T15:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 240
        }
      }
    }
    "delivery cost NOT subject to multiplier(1.0f) influenced by delivery time " when {
      "delivery time is on Tuesday between 15:00 and 19:00 inclusive" in {
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-16T15:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 200
        }
      }
    }
    "return correct gross delivery fee" when{
      "number of items are 0" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 0, "time": "2024-01-16T15:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 0
        }
      }
      "cart value is 0" in{
        val requestEntity = """{"cart_value": 0, "delivery_distance": 1000, "number_of_items": 0, "time": "2024-01-16T15:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 0
        }
      }
      "maximum delivery fee is exceeded " in{
        val requestEntity = """{"cart_value": 100, "delivery_distance": 10000, "number_of_items": 100, "time": "2024-01-16T15:00:00Z"}"""
        Post("/api/calculate-delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> DeliveryApiRoutes.route ~> check {
          val response = responseAs[DeliveryFeeResponse]
          response.delivery_fee shouldEqual 1500
        }
      }
    }
  }
  "HTTP Request to all other paths" should {
    "return a 'not found' response for unsupported endpoints" when {
      "root endpoint requested" in {
        val unsupportedEndpoint = "/"
        Get(unsupportedEndpoint) ~> DeliveryApiRoutes.route ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
      "api root endpoint requested" in {
        val unsupportedEndpoint = "/api"
        Get(unsupportedEndpoint) ~> DeliveryApiRoutes.route ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
  }
}