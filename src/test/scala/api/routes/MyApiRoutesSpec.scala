package api.routes

import akka.http.javadsl.model.ws.Message
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.{MalformedRequestContentRejection, UnsupportedRequestContentTypeRejection}
import akka.http.scaladsl.testkit._
import api.Main.myApiRoutes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MyApiRoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  val myApiRoutes = new MyApiRoutes

  "MyApiRoutes" should {

    "return a success message for POST requests to /api/delivery-fee" in {
      val requestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, requestEntity) ~> myApiRoutes.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "Request received successfully!"
      }
    }
    "return a bad request for POST requests with invalid time to /api/delivery-fee" when {
      "string is not even time" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "invalid-time"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide the time in UTC ISO format."
        }
      }
      "Time is almost like in ISO but missing T and Z" in {
        val invalidRequestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-1513:00:00"}"""

        Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual "Invalid time format. Please provide the time in UTC ISO format."
        }
      }
    }
    "return a bad request:malformed for POST requests missing required members" when {
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
    "return a bad request:malformed for POST requests with wrong types for fields" in{
      val invalidRequestEntity = """{"cart_value": "790", "delivery_distance": 2235, "number_of_items": 4, "time": "invalid-time"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`application/json`, invalidRequestEntity) ~> myApiRoutes.route ~> check {
        rejection should matchPattern {
          case MalformedRequestContentRejection("Expected Int as JsNumber, but got \"790\"", _: spray.json.DeserializationException) =>
        }
      }
    }
    "return rejection for POST requests with headers other than JSON for example text/plain" in {
      val requestEntity = """{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}"""

      Post("/api/delivery-fee").withEntity(ContentTypes.`text/plain(UTF-8)`, requestEntity) ~> myApiRoutes.route ~> check {
        rejections shouldNot be(empty)
//        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}

