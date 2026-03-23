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

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.{SchemaRegistry, SpecificationVersion}

import javax.inject.Singleton
import scala.io.Source

@Singleton
class ValidationService {
  private val schemaMapper: ObjectMapper = new ObjectMapper()

  private val schemaRegistry: SchemaRegistry =
    SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4)

  def get(schemaFile: String): Validator = {
    val source               = Source.fromInputStream(getClass.getResourceAsStream(schemaFile))
    val schemaJsonFileString = source.mkString
    source.close()
    val schemaNode           = schemaMapper.readTree(schemaJsonFileString)
    val schema               = schemaRegistry.getSchema(schemaNode)

    new Validator(schema)
  }

}
