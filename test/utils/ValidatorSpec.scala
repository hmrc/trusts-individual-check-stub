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

import models.{FailedValidation, SuccessfulValidation}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ValidatorSpec extends AnyWordSpec with Matchers {

  private val schema              = "/resources/schemas/API1585_Individual_Match_0.2.0.json"
  private val validationService   = new ValidationService
  private val validator           = validationService.get(schema)

  "Validator" should {

    "return SuccessfulValidation for valid JSON" in {
      val validJson = """{"nino":"AA100001A","forename":"Adam","surname":"Conder","birthDate":"2020-05-10"}"""

      val result = validator.validateAgainstSchema(validJson)

      result shouldBe SuccessfulValidation
    }

    "return FailedValidation for invalid JSON schema" in {
      val invalidJson = """{"nino":"AA100001A"}"""

      val result = validator.validateAgainstSchema(invalidJson)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Invalid Json"
    }

    "return FailedValidation for malformed JSON" in {
      val malformedJson = """{"nino":"AA100001A", invalid json"""

      val result = validator.validateAgainstSchema(malformedJson)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Not JSON"
    }

    "return FailedValidation for JSON with duplicate keys" in {
      val duplicateKeyJson = """{"nino":"AA100001A","nino":"AA100002A","forename":"Adam","surname":"Conder","birthDate":"2020-05-10"}"""

      val result = validator.validateAgainstSchema(duplicateKeyJson)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Not JSON"
    }

    "return FailedValidation for non-JSON string" in {
      val nonJson = "this is not json at all"

      val result = validator.validateAgainstSchema(nonJson)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Not JSON"
    }

    "return FailedValidation for empty string" in {
      val emptyString = ""

      val result = validator.validateAgainstSchema(emptyString)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Not JSON"
    }

    "return FailedValidation for JSON with invalid field format" in {
      val invalidFieldJson = """{"nino":"AA100001A","forename":"Adam","surname":"Conder","birthDate":"not-a-date"}"""

      val result = validator.validateAgainstSchema(invalidFieldJson)

      result shouldBe a[FailedValidation]
      result.asInstanceOf[FailedValidation].message shouldBe "Invalid Json"
    }
  }

}
