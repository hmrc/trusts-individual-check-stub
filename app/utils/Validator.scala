/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema.Schema
import models.{FailedValidation, SuccessfulValidation, ValidationError, ValidationResult}
import play.api.Logger

import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.{Success, Try}

class Validator(schema: Schema) {

  private val logger: Logger = Logger(getClass)

  def validateAgainstSchema(input: String): ValidationResult =

    try {
      val jsonToValidate: Try[JsonNode] = doNotAllowDuplicatedProperties(input)

      jsonToValidate match {
        case Success(json) =>
          val validationOutput = schema.validate(json)

          if (validationOutput.isEmpty) {
            SuccessfulValidation
          } else {
            val validationErrors = getValidationErrors(validationOutput)
            val failedValidation = FailedValidation("Invalid Json", 0, validationErrors)

            logger.info(validationErrors.mkString)
            logger.info("Failed schema validation")
            logger.debug(failedValidation.toString)

            failedValidation
          }

        case _ =>
          logger.error(s"[Failure]Error validating Json request against schemas")
          FailedValidation("Not JSON", 0, Nil)
      }
    } catch {
      case ex: Exception =>
        logger.error(s"Error validating Json request against schemas: ${ex.getMessage}")
        FailedValidation("Not JSON", 0, Nil)
    }

  private def getValidationErrors(
    validationOutput: java.util.List[com.networknt.schema.Error]
  ): Seq[ValidationError] = {
    val validationErrors = validationOutput.asScala.toList
      .map { err =>
        val msg = err.getMessage
        val loc = err.getInstanceLocation.toString
        ValidationError(msg, loc)
      }
    val locations        = validationErrors.map(e => s"${e.location} -> ${e.message}")
    logger.error(s"[Validator][getValidationErrors] validationErrors failed at locations :  $locations")
    println("validationErrors" + validationErrors)
    validationErrors
  }

  private def doNotAllowDuplicatedProperties(jsonNodeAsString: String): Try[JsonNode] = {
    val objectMapper: ObjectMapper = new ObjectMapper()
    objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
    Try {
      val parser             = objectMapper.getFactory.createParser(jsonNodeAsString)
      val jsonNode: JsonNode = objectMapper.readTree(parser)
      jsonNode
    }
  }

}
