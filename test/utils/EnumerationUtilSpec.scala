/*
 * Copyright 2016 HM Revenue & Customs
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

import controllers.FakeCCApplication
import mappings.Periods.Period
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 29/01/2016.
 */
class EnumerationUtilSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "EnumerationUtil" should {

    "return a JsError when it cannot parse json object" in {
      val json = Json.parse(
        """
          |{
          | "enum" : "something"
          |}
        """.stripMargin)
      json.validate[Period] match {
        case JsSuccess(v, _) =>
          !v.isInstanceOf[Period]
        case JsError(errors) =>
          errors.head._2.head.message shouldBe "String value expected"
      }
    }

    "return a JsError when it cannot parse json string" in {
      val json = Json.parse(
        """
          |"something"
        """.stripMargin)
      json.validate[Period] match {
        case JsSuccess(v, _) =>
          !v.isInstanceOf[Period]
        case JsError(errors) =>
          errors.head._2.head.message shouldBe "Enumeration expected of type: 'class mappings.Periods$', but it does not appear to contain the value: 'something'"
      }
    }

  }

}
