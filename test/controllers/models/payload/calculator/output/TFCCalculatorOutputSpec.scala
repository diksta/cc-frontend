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

package controllers.models.payload.calculator.output

import models.payload.calculator.output.CalculatorOutput
import models.payload.calculator.output.tfc.{TFCOutputChild, TFCPeriod, Contribution, TFCCalculatorOutput}
import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class TFCCalculatorOutputSpec extends UnitSpec {

  "read a valid TFC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
        {
          "calculation" : {
            "tfc": {
              "from": "2016-08-27",
              "until": "2016-11-27",
              "householdContribution": {
                "parent": 8500.00,
                "government": 500.00,
                "totalChildCareSpend": 9000.00
              },
              "numberOfPeriods" : 1,
              "periods" : [
                {
                  "from": "2016-08-27",
                  "until": "2016-11-27",
                  "periodContribution": {
                    "parent": 8500.00,
                    "government": 500.00,
                    "totalChildCareSpend": 9000.00
                  },
                  "children": [
                    {
                      "id": 0,
                      "name" : "Child 1",
                      "childCareCost": 3000.00,
                      "childContribution" : {
                        "parent": 8500.00,
                        "government": 500.00,
                        "totalChildCareSpend": 9000.00
                      },
                      "timeToMaximizeTopUp" : 0,
                      "failures" : []
                    }
                  ]
                }
              ]
            },
            "esc": null,
            "tc": null
          }
        }
        """.stripMargin
    )

    val result = json.validate[CalculatorOutput]

    result match {
      case JsSuccess(x, _) => {
        x shouldBe a[CalculatorOutput]
        x.calculation should not be null

        x.calculation.tfc.isInstanceOf[Option[TFCCalculatorOutput]] shouldBe true

        x.calculation.tfc.head.householdContribution.isInstanceOf[Contribution] shouldBe true
        x.calculation.tfc.head.numberOfPeriods.isInstanceOf[Short] shouldBe true
        x.calculation.tfc.head.periods.isInstanceOf[List[TFCPeriod]] shouldBe true
        x.calculation.tfc.head.from shouldBe a[LocalDate]
        x.calculation.tfc.head.until shouldBe a[LocalDate]

        x.calculation.tfc.head.householdContribution.government.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.householdContribution.parent.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.householdContribution.totalChildCareSpend.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.tfc.head.periods.head.periodContribution.isInstanceOf[Contribution] shouldBe true
        x.calculation.tfc.head.periods.head.children.isInstanceOf[List[TFCOutputChild]] shouldBe true
        x.calculation.tfc.head.periods.head.from shouldBe a[LocalDate]
        x.calculation.tfc.head.periods.head.until shouldBe a[LocalDate]

        x.calculation.tfc.head.periods.head.periodContribution.government.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.periods.head.periodContribution.parent.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.periods.head.periodContribution.totalChildCareSpend.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.tfc.head.periods.head.children.head.childCareCost.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.childContribution.isInstanceOf[Contribution] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.failures.isInstanceOf[List[String]] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.id.isInstanceOf[Short] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.name.isInstanceOf[Option[String]] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.timeToMaximizeTopUp.isInstanceOf[Short] shouldBe true

        x.calculation.tfc.head.periods.head.children.head.childContribution.government.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.childContribution.parent.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tfc.head.periods.head.children.head.childContribution.totalChildCareSpend.isInstanceOf[BigDecimal] shouldBe true

      }
      case _ => throw new Exception

    }

  }
}
