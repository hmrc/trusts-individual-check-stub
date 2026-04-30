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

import com.networknt.schema.{SchemaRegistry, SpecificationVersion}

import java.io.InputStream
import javax.inject.Singleton
import scala.io.Source
import scala.util.Using

@Singleton
class ValidationService {

  def get(schemaFile: String): Validator = {

    val resource = resourceAsString(schemaFile)
      .getOrElse(throw new RuntimeException("Missing schema: " + schemaFile))

    val schema = SchemaRegistry
      .withDefaultDialect(SpecificationVersion.DRAFT_4)
      .getSchema(resource)

    new Validator(schema)
  }

  private def resourceAsString(resourcePath: String): Option[String] =
    resourceAsInputStream(resourcePath) flatMap { is =>
      Using(Source.fromInputStream(is))(_.getLines().mkString("\n")).toOption
    }

  private def resourceAsInputStream(resourcePath: String): Option[InputStream] =
    Option(getClass.getResourceAsStream(resourcePath))

}
