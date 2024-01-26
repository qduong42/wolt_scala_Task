package api.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit._
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
  }
}
