package api.routes

import akka.http.javadsl.model.ws.Message
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ContentTypes, HttpHeader, HttpMethods, StatusCodes}
import akka.http.scaladsl.server.{MalformedRequestContentRejection, MethodRejection, UnacceptedResponseContentTypeRejection, UnsupportedRequestContentTypeRejection}
import akka.http.scaladsl.testkit._
import api.Main.myApiRoutes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MyApiRoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  val myApiRoutes = new MyApiRoutes

  "HTTP Request to /api/calculate-delivery-fee" should {
    "be rejected for other requests than POST request" in {
      Get("/api/delivery-fee") ~> myApiRoutes.route ~> check{
        rejection shouldEqual MethodRejection(HttpMethods.POST)
//        status shouldEqual(StatusCodes.UnsupportedMediaType)
      }
    }
    "return a success message for POST request" in {
      val requestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
        // Parse the response body into a case class
        val response = responseAs[DeliveryFeeResponse]
        // Assert the expected structure
        status shouldEqual StatusCodes.OK
      }
    }
    "return a response in application/json for POST request" in{
      val requestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {

        val contentTypeHeader: Option[HttpHeader] = header[`Content-Type`]

        contentTypeHeader.exists(_.value == ContentTypes.`application/json`.value) shouldEqual true

      }
    }
    "return a bad request for POST requests with invalid time in body" when {
      "random string input not remotely related to time" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide the time in UTC ISO format."
        }
      }
      "time is almost like in ISO but missing T and Z" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-1513:00:00"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide the time in UTC ISO format."
        }
      }
    }
    "return a bad request:malformed for POST requests missing required members in body" when {
      "cart_value is missing in POST request body" in {
        val invalidRequestEntity = """{"delivery_distance": 2235, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'cart_value'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "delivery_distance is missing in POST request body" in{
        val invalidRequestEntity = """{"cart_value": 790, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'delivery_distance'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "number_of_items is missing in POST request body" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "time": "invalid-time"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'number_of_items'", _: spray.json.DeserializationException) =>
          }
        }
      }
      "time is missing in POST request body" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          rejection should matchPattern {
            case MalformedRequestContentRejection("Object is missing required member 'time'", _: spray.json.DeserializationException) =>
          }
        }
      }
    }
    "return a bad request:malformed for POST requests with wrong types for fields in body" in{
      val invalidRequestEntity = """{"cart_value": "790", "delivery_distance": 2235, "number_of_items": 4, "time": "invalid-time"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
        rejection should matchPattern {
          case MalformedRequestContentRejection("Expected Int as JsNumber, but got \"790\"", _: spray.json.DeserializationException) =>
        }
      }
    }
    "Unsupported Media Type response for POST requests with headers other than JSON for example text/plain" in {
      val requestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`text/plain(UTF-8)`, requestEntity) ~> myApiRoutes.route ~> check {
//        rejections shouldNot be(empty)
//        rejection shouldEqual UnsupportedRequestContentTypeRejection(Set(ContentType(application/json)))
//        rejection should matchPattern {
//          case UnsupportedRequestContentTypeRejection(contentTypes) if contentTypes.contains(ContentTypes.`application/json`) =>
//        }
        status shouldEqual(StatusCodes.UnsupportedMediaType)
      }
    }
    "return correct delivery distance fee" when{
      "delivery distance is 0" in {
        val requestEntity = """{"cart_value": 790, "delivery_distance": 0, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 0
        }
      }
      "delivery distance is 1 - 500" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 499, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 200
        }
      }
      "delivery distance is 1000" in {
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 200
        }
      }
      "delivery distance is 1999" in {
      val requestEntity = """{"cart_value": 1000, "delivery_distance": 1999, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
        // Parse the response body into a case class
        val response = responseAs[DeliveryFeeResponse]
        // Assert the expected structure
        response.delivery_fee shouldEqual 400
      }
    }
      "delivery distance is 2000" in {
      val requestEntity = """{"cart_value": 1000, "delivery_distance": 2000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
        // Parse the response body into a case class
        val response = responseAs[DeliveryFeeResponse]
        // Assert the expected structure
        response.delivery_fee shouldEqual 400
      }
    }
    }
    "return correct delivery fee influenced by cart value" when{
      "cart value below MinimumCartValueNoSurcharge" in{
        val requestEntity = """{"cart_value": 500, "delivery_distance": 2000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 900
        }
      }
      "cart value equals MinimumCartValueNoSurcharge" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 2000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 400
        }
      }
      "cart value more than MinimumCartValueNoSurcharge" in{
        val requestEntity = """{"cart_value": 1500, "delivery_distance": 2000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 400
        }
      }
      "cart value equals CartValueNeededForFreeDelivery" in{
        val requestEntity = """{"cart_value": 20000, "delivery_distance": 5000, "number_of_items": 9, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 0
        }
      }
    }
    "return correct delivery fee influenced by number of items" when{
      "items equals MaximumNumberOfItemsNoSurcharge" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 200
        }
      }
      "items more than MaximumNumberOfItemsNoSurcharge but equal to MaximumNumberOfItemsNoBulkSurcharge" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 12, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 600
        }
      }
      "items more than both MaximumNumberOfItemsNoSurcharge and MaximumNumberOfItemsNoBulkSurcharge" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 14, "time": "2024-01-15T13:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 820
        }
      }
    }
    "have a 1.2x multiplier to fee influenced by delivery time" when{
      "delivery time is on Friday between 15:00 and 19:00 inclusive" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-19T15:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 240
        }
      }
    }
    "not have a multiplier(1.0f) influenced by delivery time " when{
      "delivery time is on Tuesday between 15:00 and 19:00 inclusive" in{
        val requestEntity = """{"cart_value": 1000, "delivery_distance": 1000, "number_of_items": 4, "time": "2024-01-16T15:00:00Z"}"""
        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
          // Parse the response body into a case class
          val response = responseAs[DeliveryFeeResponse]
          // Assert the expected structure
          response.delivery_fee shouldEqual 200
        }
      }
    }
  }
}
