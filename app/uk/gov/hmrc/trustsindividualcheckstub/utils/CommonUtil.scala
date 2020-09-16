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

package uk.gov.hmrc.trustsindividualcheckstub.utils

import play.api.libs.json.{JsValue, Json}

object CommonUtil {

  val successfulMatch = "AA100001A"
  val serviceUnavailable = "AA100503A"
  val serverError = "AA100500A"
  val notFound = "AA100404A"

  val jsonResponse400: JsValue = Json.obj(
    "failures" -> Json.arr(
      Json.obj(
        "code" -> "INVALID_PAYLOAD",
        "reason" -> "Submission has not passed validation. Invalid payload."
      )
    )
  )

  val jsonResponse400CorrelationId: JsValue = Json.obj("failures" -> Json.arr(Json.parse(
    s"""
       |{
       | "code": "INVALID_CORRELATIONID",
       | "reason": "Submission has not passed validation. Invalid Header CorrelationId."
       |}""".stripMargin)))

  val jsonResponse404: JsValue = Json.obj("failures" -> Json.arr(Json.parse(
    s"""
       |{
       | "code": "RESOURCE_NOT_FOUND",
       | "reason": "The remote endpoint has indicated that no data can be found."
       |}""".stripMargin)))

  val jsonResponse503: JsValue = Json.obj("failures" -> Json.arr(Json.parse(
    s"""
       |{
       | "code": "SERVICE_UNAVAILABLE",
       | "reason": "Dependent systems are currently not responding."
       |}""".stripMargin)))

  val jsonResponse500: JsValue = Json.obj("failures" -> Json.arr(Json.parse(
    s"""
       |{
       | "code": "SERVER_ERROR",
       | "reason": "IF is currently experiencing problems that require live service intervention."
       |}""".stripMargin)))

}
