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
import models.payload.calculator.output.esc._
import org.joda.time.LocalDate
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.json._
import mappings.Periods

class ESCCalculatorOutputSpec extends UnitSpec{

  "read a valid ESC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
        {
          "calculation" : {
            "esc": {
              "from": "2017-08-27",
              "until": "2018-04-06",
              "totalSavings": {
                "totalSaving": 104.16,
                "taxSaving": 99.2,
                "niSaving": 4.96
              },
              "taxYears": [
                {
                  "from": "2017-08-27",
                  "until": "2018-04-06",
                  "totalSavings": {
                    "totalSaving": 104.16,
                    "taxSaving": 99.2,
                    "niSaving": 4.96
                  },
                  "claimants": [
                    {
                      "qualifying": false,
                      "eligibleMonthsInTaxYear": 2,
                      "isPartner": false,
                      "escAmount": 200,
                      "escAmountPeriod": "Month",
                      "escStartDate": "2012-08-27",
                      "maximumRelief": 124,
                      "maximumReliefPeriod": "Month",
                      "income": {
                        "taxablePay": 50000,
                        "gross": 50000,
                        "taxCode": "1100L",
                        "niCategory": "A"
                      },
                      "elements": {
                        "vouchers": false
                      },
                      "savings": {
                        "totalSaving": 104.16,
                        "taxSaving": 99.2,
                        "niSaving": 4.96
                      },
                      "taxAndNIBeforeSacrifice": {
                        "taxPaid": 766.60,
                        "niPaid": 361
                      },
                      "taxAndNIAfterSacrifice": {
                        "taxPaid": 717.0,
                        "niPaid": 358.52
                      }
                    }
                  ]
                }
              ]
            },
            "tc": null,
            "tfc": null
          }
        }
       """.stripMargin
    )

    val result = json.validate[CalculatorOutput]

    result match {
      case JsSuccess(x, _) => {
        x shouldBe a[CalculatorOutput]
        x.calculation should not be null

        x.calculation.esc.isInstanceOf[Option[ESCCalculatorOutput]] shouldBe true

        //TaxYear model
        x.calculation.esc.head.taxYears.isInstanceOf[List[ESCTaxYear]] shouldBe true
        x.calculation.esc.head.from shouldBe a[LocalDate]
        x.calculation.esc.head.until shouldBe a[LocalDate]
        x.calculation.esc.head.totalSavings.isInstanceOf[Savings] shouldBe true

        x.calculation.esc.head.totalSavings.niSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.totalSavings.taxSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.totalSavings.totalSaving.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.esc.head.taxYears.head.from shouldBe a[LocalDate]
        x.calculation.esc.head.taxYears.head.until shouldBe a[LocalDate]
        x.calculation.esc.head.taxYears.head.claimants.isInstanceOf[List[ESCOutputClaimant]] shouldBe true
        x.calculation.esc.head.taxYears.head.totalSavings.isInstanceOf[Savings] shouldBe true

        x.calculation.esc.head.taxYears.head.totalSavings.niSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.totalSavings.taxSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.totalSavings.totalSaving.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.eligibleMonthsInTaxYear.isInstanceOf[Int] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.escAmount.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.escAmountPeriod.isInstanceOf[Periods.Period] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.isPartner.isInstanceOf[Boolean] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.maximumRelief.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.maximumReliefPeriod.isInstanceOf[Periods.Period] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.qualifying.isInstanceOf[Boolean] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.elements.isInstanceOf[ClaimantElements] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.escStartDate shouldBe a[LocalDate]
        x.calculation.esc.head.taxYears.head.claimants.head.income.isInstanceOf[Income] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.savings.isInstanceOf[Savings] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIAfterSacrifice.isInstanceOf[TaxAndNI] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIBeforeSacrifice.isInstanceOf[TaxAndNI] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.elements.vouchers.isInstanceOf[Boolean] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.income.gross.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.income.niCategory.isInstanceOf[String] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.income.taxablePay.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.income.taxCode.isInstanceOf[String] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.savings.niSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.savings.taxSaving.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.savings.totalSaving.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIAfterSacrifice.niPaid.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIAfterSacrifice.taxPaid.isInstanceOf[BigDecimal] shouldBe true

        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIBeforeSacrifice.niPaid.isInstanceOf[BigDecimal] shouldBe true
        x.calculation.esc.head.taxYears.head.claimants.head.taxAndNIBeforeSacrifice.taxPaid.isInstanceOf[BigDecimal] shouldBe true

      }
      case _ => throw new Exception
    }
  }

}
