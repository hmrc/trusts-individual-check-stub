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

package utils

import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.Logger

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.io.Source
import scala.util.{Success, Try}

class ValidationService () {

  private val factory = JsonSchemaFactory.byDefault()

  def get(schemaFile: String): Validator = {
    val schemaJsonFileString = Source.fromFile(getClass.getResource(schemaFile).getPath).mkString
    val schemaJson = JsonLoader.fromString(schemaJsonFileString)
    val schema = factory.getJsonSchema(schemaJson)
    new Validator(schema)
  }

}

class Validator(schema: JsonSchema) {
  private val jsonErrorMessageTag = "message"
  private val jsonErrorInstanceTag = "instance"
  private val jsonErrorPointerTag = "pointer"

  private val logger: Logger = Logger(getClass)

  def validateAgainstSchema(input: String): ValidationResult = {

    try {
      val jsonToValidate: Try[JsonNode] = doNotAllowDuplicatedProperties(input)

      jsonToValidate match {
        case Success(json) =>
          val validationOutput: ProcessingReport = schema.validate(json, true)

          if (validationOutput.isSuccess) {
            SuccessfulValidation
          } else {
            val validationErrors = getValidationErrors(validationOutput)
            val failedValidation = FailedValidation("Invalid Json", 0, validationErrors)

            logger.info(validationErrors.mkString)
            logger.info("Failed schema validation")
            logger.debug(failedValidation.toString)

            failedValidation
          }

        case _=> logger.error(s"[Failure]Error validating Json request against schemas")
          FailedValidation("Not JSON", 0, Nil)
      }
    }
    catch {
      case ex: Exception =>
        logger.error(s"Error validating Json request against schemas: ${ex.getMessage}")
        FailedValidation("Not JSON", 0, Nil)
    }
  }


  private def getValidationErrors(validationOutput: ProcessingReport): Seq[ValidationError] = {
    validationOutput.iterator
      .asScala
      .toList
      .filter(_.getLogLevel == ERROR)
      .map { m =>
        val error = m.asJson()
        val message = error.findValue(jsonErrorMessageTag).asText("")
        val location = error.findValue(jsonErrorInstanceTag).at(s"/$jsonErrorPointerTag").asText()
        val locations = error.findValues(jsonErrorInstanceTag)
        logger.error(s"[getValidationErrors] Failed at locations : ${locations}")
        ValidationError(message, if (location == "") "/" else location)
      }
  }

  private def doNotAllowDuplicatedProperties(jsonNodeAsString: String): Try[JsonNode] = {
    val objectMapper: ObjectMapper = new ObjectMapper()
    objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)

    val jsonFactory: JsonFactory = objectMapper.getFactory
    val jsonParser: JsonParser = jsonFactory.createParser(jsonNodeAsString)

    objectMapper.readTree(jsonParser)

    val jsonAsNode: Try[JsonNode] = Try(JsonLoader.fromString(jsonNodeAsString))
    jsonAsNode
  }
}

