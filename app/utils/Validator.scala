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
import com.networknt.schema.{Error, InputFormat, Schema}
import models.{FailedValidation, SuccessfulValidation, ValidationError, ValidationResult}
import play.api.Logger

import scala.jdk.CollectionConverters.IterableHasAsScala

class Validator(schema: Schema) {

  private val logger: Logger = Logger(getClass)

  private def validateInternal(subject: String): List[Error] =
    schema.validate(subject, InputFormat.JSON).asScala.toList

  def validateAgainstSchema(input: String): ValidationResult =
    try {
      val jsonToValidate: JsonNode      = doNotAllowDuplicatedProperties(input)
      val validationOutput: List[Error] = validateInternal(jsonToValidate.toString)

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
    } catch {
      case ex: Exception =>
        logger.error(s"Error validating Json request against schemas: ${ex.getMessage}")
        FailedValidation("Not JSON", 0, Nil)
    }

  private def getValidationErrors(validationOutput: List[Error]): Seq[ValidationError] =
    validationOutput.map { error =>
      val message  = error.getMessage
      val location = error.getInstanceLocation.toString
      logger.error(s"[getValidationErrors] Failed at locations : $location")
      ValidationError(message, if (location == "") "/" else location)
    }

  private def doNotAllowDuplicatedProperties(jsonNodeAsString: String): JsonNode = {
    val objectMapper: ObjectMapper = new ObjectMapper()
    objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
    objectMapper.readTree(jsonNodeAsString)
  }

}
