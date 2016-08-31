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

package controllers.models.payload.calculator.input

import models.payload.calculator.input.{CalculatorInput, CalculatorPayload, Eligibility}
import models.payload.calculator.input.esc._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import mappings.Periods

class ESCCalculatorSpec extends UnitSpec{

  "Return a valid request for ESC calculator" in {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val from = LocalDate.parse("2016-08-27", formatter)
    val until = LocalDate.parse("2017-08-27", formatter)

    val escElements = ESCElements (
      vouchers = true
    )

    val escIncome = ESCIncome (
      taxablePay = 50000.00,
      gross = 50000.00,
      taxCode = "1100L",
      niCategory = "A"
    )
    val escClaimant = List(ESCClaimant (
      qualifying = false,
      isPartner = false,
      eligibleMonthsInPeriod = 2,
      income = escIncome,
      elements = escElements,
      escStartDate = from,
      escAmount = 200.0,
      escAmountPeriod = Periods.Monthly
    ))

    val escPeriods = List(ESCPeriod (
      from = from,
      until = until,
      claimants = escClaimant
    ))

    val escTaxYear = ESCTaxYear (
      startDate = from,
      endDate = until,
      periods = escPeriods
    )

    val escEligibility = ESCEligibility (
      escTaxYear = List(escTaxYear)
    )

    val eligibility = Eligibility (
      tc = None,
      tfc = None,
      esc = Some(escEligibility)
    )

    val escCalculator = CalculatorInput(CalculatorPayload(eligibility))

    val outputJson = Json.parse(
      s"""
         {
           "payload": {
             "eligibility": {
               "esc": {
                 "taxYears": [
                   {
                     "startDate": "2016-08-27",
                     "endDate": "2017-08-27",
                     "periods": [
                       {
                         "from": "2016-08-27",
                         "until": "2017-08-27",
                         "claimants": [
                           {
                             "qualifying": false,
                             "isPartner": false,
                             "eligibleMonthsInPeriod": 2,
                             "income": {
                               "taxablePay": 50000.0,
                               "gross": 50000.0,
                               "taxCode": "1100L",
                               "niCategory": "A"
                             },
                             "elements": {
                               "vouchers": true
                             },
                             "escAmount": 200.0,
                             "escAmountPeriod": "Month",
                             "escStartDate": "2016-08-27"
                           }
                         ]
                       }
                     ]
                   }
                 ]
               },
               "tc": null,
               "tfc": null
             }
           }
         }
         """.stripMargin)

    val result = Json.toJson[CalculatorInput](escCalculator)
    result shouldBe outputJson
  }
}
