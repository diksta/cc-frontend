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
import models.payload.calculator.output.tc.{Elements, TCPeriod, TCTaxYear, TCCalculatorOutput}
import org.joda.time.LocalDate
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.json._

class TCCalculatorOutputSpec extends UnitSpec {

  "read a valid TC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
          {
          "calculation": {
           "tc": {
             "from": "2016-09-27",
             "until": "2017-04-06",
             "totalAwardAmount": 2989.74,
             "totalAwardProRataAmount" :0.00,
             "houseHoldAdviceAmount": 0.00,
             "totalHouseHoldAdviceProRataAmount" :0.00,
             "taxYears": [
             {
                 "from": "2016-09-27",
                 "until": "2017-04-06",
                 "taxYearAwardAmount": 2989.74,
                 "taxYearAwardProRataAmount" : 0.00,
                 "taxYearAdviceAmount": 0.00,
                 "taxYearAdviceProRataAmount" : 0.00,
                 "periods": [
                   {
                    "from": "2016-09-27",
                    "until": "2016-12-12",
                     "periodNetAmount": 2989.74,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                        "wtcWorkElement": {
                          "netAmount": 92.27,
                          "maximumAmount": 995.60,
                          "taperAmount": 903.33
                        },
                        "wtcChildcareElement": {
                          "netAmount": 704.87,
                          "maximumAmount": 704.87,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 2078.60,
                          "maximumAmount": 2078.60,
                          "taperAmount": 0.00
                        },
                        "ctcFamilyElement": {
                          "netAmount": 114.00,
                          "maximumAmount": 114.00,
                          "taperAmount": 0.00
                        }
                      }
                  },
                  {
                    "from": "2016-12-12",
                    "until": "2017-04-06",
                    "periodNetAmount": 0.00,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                      "wtcWorkElement": {
                          "netAmount": 0.00,
                          "maximumAmount":  872.85,
                          "taperAmount":  872.85
                        },
                        "wtcChildcareElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                          },
                        "ctcFamilyElement": {
                          "maximumAmount": 0.00,
                          "netAmount": 0.00,
                          "taperAmount": 0.00
                        }
                      }
                    }
                 ]
              }
             ]
           },
           "tfc": null,
           "esc": null
          }
          }
          """.stripMargin
    )

    val result = json.validate[CalculatorOutput]

    result match {
      case JsSuccess(x, _) => {
        x shouldBe a[CalculatorOutput]
        x.calculation should not be null

        x.calculation.tc.isInstanceOf[Option[TCCalculatorOutput]] shouldBe true

        x.calculation.tc.head.from shouldBe a[LocalDate]
        x.calculation.tc.head.until shouldBe a[LocalDate]
        x.calculation.tc.head.houseHoldAdviceAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.proRataEnd.isInstanceOf[Option[LocalDate]] shouldBe true
        x.calculation.tc.head.totalAwardAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.totalAwardProRataAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.totalHouseHoldAdviceProRataAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.isInstanceOf[List[TCTaxYear]] shouldBe true

        x.calculation.tc.head.taxYears.head.from shouldBe a[LocalDate]
        x.calculation.tc.head.taxYears.head.until shouldBe a[LocalDate]
        x.calculation.tc.head.taxYears.head.taxYearAdviceAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.taxYearAdviceProRataAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.taxYearAwardAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.taxYearAwardProRataAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.isInstanceOf[List[TCPeriod]] shouldBe true

        x.calculation.tc.head.taxYears.head.periods.head.from shouldBe a[LocalDate]
        x.calculation.tc.head.taxYears.head.periods.head.until shouldBe a[LocalDate]
        x.calculation.tc.head.taxYears.head.periods.head.periodAdviceAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.periodNetAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.isInstanceOf[Elements] shouldBe true

        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcWorkElement.maximumAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcWorkElement.netAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcWorkElement.taperAmount.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcChildcareElement.maximumAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcChildcareElement.netAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.wtcChildcareElement.taperAmount.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcFamilyElement.maximumAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcFamilyElement.netAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcFamilyElement.taperAmount.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcIndividualElement.maximumAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcIndividualElement.netAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.tc.head.taxYears.head.periods.head.elements.ctcIndividualElement.taperAmount.isInstanceOf[BigDecimal] shouldBe true

      }
      case _ => throw new Exception

    }

  }
}
