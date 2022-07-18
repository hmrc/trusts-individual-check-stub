/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import play.api.test.Helpers._
import play.api.test.FakeRequest
import utils.CommonUtil

class IndividualsControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val ENVIRONMENT_HEADER = "Environment"
  val TOKEN_HEADER = "Authorization"
  val CORRELATIONID_HEADER = "CorrelationId"

  val CONTENT_TYPE_HEADER = ("Content-type", "application/json")

  private val application = baseApplicationBuilder
    .configure(("metrics.enabled", false))
    .build()

  private def createRequestWithValidHeaders( body: JsValue, url :String = "/trusts/variations", method:String = "POST"): FakeRequest[AnyContentAsJson] = {
    createRequestWithoutHeaders(body)
      .withHeaders((ENVIRONMENT_HEADER, "dev"), (TOKEN_HEADER, "Bearer 11"), (CORRELATIONID_HEADER, "cd7a4033-ae84-4e18-861d-9d62c6741e87"))
  }

  private def createRequestWithoutHeaders(body: JsValue): FakeRequest[AnyContentAsJson] = {
    FakeRequest("POST", "/individual/match")
      .withHeaders(CONTENT_TYPE_HEADER)
      .withJsonBody(body)
  }

  private def body(nino: String): JsValue = Json.parse(s"""{
                          |    "nino": "$nino",
                          |    "forename": "Adam",
                          |    "surname": "Conder",
                          |    "birthDate": "2020-05-10"
                          |}""".stripMargin)

  private val controller = application.injector.instanceOf[IndividualsController]

  "POST /individuals/match" should {
    "return 200 with a valid body" when {
      "a match is found" in {

        val fakeRequest = createRequestWithValidHeaders(body(CommonUtil.successfulMatch))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.OK
        contentAsJson(result) shouldBe Json.obj("individualMatch" -> true)
      }

      "a match is not found" in {

        val fakeRequest = createRequestWithValidHeaders(body("AA000000A"))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.OK
        contentAsJson(result) shouldBe Json.obj("individualMatch" -> false)
      }
    }

    "return 400" when {
      "invalid payload" in {

        val fakeRequest = createRequestWithValidHeaders(Json.obj())

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_PAYLOAD",
            "reason" -> "Submission has not passed validation. Invalid payload."
          )
        ))
      }
      "no correlation id" in {

        val fakeRequest = createRequestWithoutHeaders(body("AA000000A"))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_CORRELATIONID",
            "reason" -> "Submission has not passed validation. Invalid Header CorrelationId."
          )
        ))
      }
      "invalid correlation id" in {

        val fakeRequest = createRequestWithoutHeaders(body("AA000000A"))
          .withHeaders(
            (ENVIRONMENT_HEADER, "dev"), (TOKEN_HEADER, "Bearer 11"), (CORRELATIONID_HEADER, "")
          )

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_CORRELATIONID",
            "reason" -> "Submission has not passed validation. Invalid Header CorrelationId."
          )
        ))
      }
    }

    "return 404" when {
      "nino corresponds with nino not found" in {

        val fakeRequest = createRequestWithValidHeaders(body(CommonUtil.notFound))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.NOT_FOUND
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(
          Json.obj(
            "code" -> "RESOURCE_NOT_FOUND",
            "reason" -> "The remote endpoint has indicated that no data can be found."
          )
        ))
      }
    }

    "return 503" when {
      "nino corresponds with service unavailable" in {

        val fakeRequest = createRequestWithValidHeaders(body(CommonUtil.serviceUnavailable))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(Json.parse(
          s"""
             |{
             | "code": "SERVICE_UNAVAILABLE",
             | "reason": "Dependent systems are currently not responding."
             |}""".stripMargin)))
      }
    }

    "return 500" when {
      "nino corresponds with server error" in {

        val fakeRequest = createRequestWithValidHeaders(body(CommonUtil.serverError))

        val result = controller.matchIndividual()(fakeRequest)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        contentAsJson(result) shouldBe Json.obj("failures" -> Json.arr(Json.parse(
          s"""
             |{
             | "code": "SERVER_ERROR",
             | "reason": "IF is currently experiencing problems that require live service intervention."
             |}""".stripMargin)))
      }
    }
  }
}
