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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.trustsindividualcheckstub.config.AppConfig
import uk.gov.hmrc.trustsindividualcheckstub.utils.CommonUtil._
import uk.gov.hmrc.trustsindividualcheckstub.utils._

@Singleton()
class IndividualsController @Inject()(appConfig: AppConfig,
                                      cc: ControllerComponents,
                                      validationService: ValidationService
                                     ) extends BackendController(cc) {

  private val logger = Logger("IndividualsController")

  def matchIndividual(): Action[AnyContent] = Action { implicit request =>

    val schema = "/resources/schemas/API1585_Individual_Match_0.2.0.json"

    request.headers.get("CorrelationId") match {
      case Some(corrId) =>

        val regex = """^[0-9a-fA-F]{8}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{12}$""".r

        if (regex.findFirstIn(corrId).isDefined) {
          val payload: JsValue = request.body.asJson.get

          val validationResult = validationService.get(schema).validateAgainstSchema(payload.toString())

          logger.info(s"[matchIndividual] payload : ${payload}.")

          response(payload, validationResult)

        } else {
          BadRequest(jsonResponse400CorrelationId)
        }
      case None => BadRequest(jsonResponse400CorrelationId)

    }

  }

  private def response(payload: JsValue, validationResult: ValidationResult): Result = {
    validationResult match {
      case fail: FailedValidation =>
        logger.info("[matchIndividual] failed in payload validation.")
        logger.error(s"Failed with errors ${Json.toJson(fail)}")
        BadRequest(jsonResponse400)
      case SuccessfulValidation =>

        logger.info(s"[matchIndividual] successful validation with payload: $payload.")

        val nino = (payload \ "nino").as[String]

        nino match {
          case `notFound` => NotFound(jsonResponse404)
          case `serviceUnavailable` => ServiceUnavailable(jsonResponse503)
          case `serverError` => InternalServerError(jsonResponse500)
          case `successfulMatch` => Ok(Json.obj("individualMatch" -> true))
          case _ => Ok(Json.obj("individualMatch" -> false))
        }

    }
  }
}
