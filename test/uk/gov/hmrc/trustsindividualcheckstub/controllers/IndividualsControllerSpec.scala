/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.trustsindividualcheckstub.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest

class IndividualsControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val application = baseApplicationBuilder
    .configure(("metrics.enabled", false))
    .build()

  private def body(nino: String): JsValue = Json.parse(s"""{
                          |    "nino": "$nino",
                          |    "forename": "Adam",
                          |    "surname": "Conder",
                          |    "birthDate": "2020-05-10"
                          |}""".stripMargin)

  private def fakeRequest(nino: String = "JP121314A") =
    FakeRequest("POST", "/")
      .withHeaders(("Content-type", "application/json"))
      .withJsonBody(body(nino))

  private val controller = application.injector.instanceOf[IndividualsController]

  "POST /individuals/match" should {
    "return 200 with a valid body" when {
      "a match is found" in {
        val result = controller.matchIndividual()(fakeRequest())
        status(result) shouldBe Status.OK
        contentAsJson(result) shouldBe Json.obj("individualMatch" -> true)
      }

      "a match is not found" in {
        val result = controller.matchIndividual()(fakeRequest("AA000000A"))
        status(result) shouldBe Status.OK
        contentAsJson(result) shouldBe Json.obj("individualMatch" -> false)
      }
    }
  }
}
