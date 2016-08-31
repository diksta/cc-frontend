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

import models.payload.calculator.input.{CalculatorInput,  Eligibility, CalculatorPayload}
import models.payload.calculator.input.tc._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import mappings.Periods

/**
 * Created by user on 15/03/16.
 */
class TCCalculatorSpec extends UnitSpec {

  "Return a valid request for TC calculator" in {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val from = LocalDate.parse("2016-09-27", formatter)
    val until = LocalDate.parse("2017-04-06", formatter)

    val tcChildElements = ChildElements(
    child = true,
    youngAdult = false,
    disability = false,
    severeDisability = false,
    childcare = true
    )

    val tcChild = Child(
      id = 0,
      name = "Child 1",
      qualifying = true,
      childcareCost = BigDecimal(200.00),
      childcareCostPeriod = Periods.Monthly,
      childElements = tcChildElements
    )

    val tcClaimantDisability = ClaimantDisability(
      disability = false,
      severeDisability = false
    )

    val tcClaimant = Claimant(
      qualifying = true,
      isPartner = false,
      claimantElements = tcClaimantDisability,
      doesNotTaper = false,
      failures = Some(List())
    )

    val tcHouseHoldElements = HouseHoldElements(
    basic = true,
    hours30 = false,
    childcare = true,
    loneParent = true,
    secondParent = false,
    family = true
    )

    val tcPeriod = Period(
      from = from,
      until = until,
      householdElements = tcHouseHoldElements,
      claimants = List(tcClaimant),
      children = List(tcChild)
    )

    val tcTaxYear = TaxYear(
      from = from,
      until = until,
      houseHoldIncome = BigDecimal(17000.00),
      periods = List(tcPeriod)
    )

    val tcEligibility = TCEligibility(
      taxYears = List(tcTaxYear),
      proRataEnd = until
      )

    val eligibility = Eligibility(
      tc = Some(tcEligibility),
      tfc = None,
      esc = None
    )

    val tcCalculator = CalculatorInput(CalculatorPayload(eligibility))

    val outputJson = Json.parse(
      s"""
        {
          "payload": {
            "eligibility": {
              "tc": {
              "proRataEnd": "2017-04-06",
                "taxYears": [
                  {
                    "from": "2016-09-27",
                    "until": "2017-04-06",
                    "houseHoldIncome" : 17000.00,
                    "periods": [
                      {
                        "from": "2016-09-27",
                        "until": "2017-04-06",
                        "householdElements": {
                          "basic": true,
                          "hours30": false,
                          "childcare": true,
                          "loneParent": true,
                          "secondParent": false,
                          "family": true
                        },
                        "claimants": [
                          {
                            "qualifying": true,
                            "isPartner": false,
                            "claimantElements": {
                              "disability": false,
                              "severeDisability": false
                            },
                            "doesNotTaper" : false,
                            "failures": []
                          }
                        ],
                        "children": [
                          {
                            "id": 0,
                            "name": "Child 1",
                            "qualifying": true,
                            "childcareCost": 200.00,
                            "childcareCostPeriod": "Month",
                            "childElements": {
                              "child": true,
                              "youngAdult": false,
                              "disability": false,
                              "severeDisability": false,
                              "childcare": true
                            }
                          }
                        ]
                      }
                    ]
                  }
                ]
              },
              "esc": null,
              "tfc": null
            }
          }
        }
         """.stripMargin)

    val result = Json.toJson[CalculatorInput](tcCalculator)
    result shouldBe outputJson
  }

}
