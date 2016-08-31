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

import models.payload.calculator.input._
import models.payload.calculator.input.tfc.{TFCPeriod, TFCEligibility}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by elsie on 11/03/16.
 */
class TFCCalculatorSpec extends UnitSpec {

  "Return a valid request for TFC calculator" in {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val from = LocalDate.parse("2016-08-27", formatter)
    val until = LocalDate.parse("2017-08-27", formatter)

    val child = List(
      Child (
        id = 1,
        name = Some("Child 1"),
        qualifying = true,
        from = from,
        until = until,
        childcareCost = 30.00,
        disability = Disability(
          disabled = false,
          severelyDisabled = false
        )
      )
    )

    val periods = List(
      TFCPeriod (
        from = from,
        until = until,
        periodEligibility = true,
        children = child
      )
    )

    val tfcEligibility = TFCEligibility (
      from = from,
      until = until,
      householdEligibility = true,
      periods = periods
    )

    val eligibility = Eligibility (
      tc = None,
      esc = None,
      tfc = Some(tfcEligibility)
    )

    val tfcCalculator = CalculatorInput(CalculatorPayload(eligibility))

    val outputJson = Json.parse(
      s"""
         {
           "payload": {
             "eligibility": {
               "esc":null,
               "tc": null,
               "tfc": {
                 "from": "2016-08-27",
                 "until": "2017-08-27",
                 "householdEligibility": true,
                 "periods": [
                   {
                     "from" : "2016-08-27",
                     "until" : "2017-08-27",
                     "periodEligibility" : true,
                     "children" : [
                       {
                         "id" : 1,
                         "name" : "Child 1",
                         "qualifying" : true,
                         "from" : "2016-08-27",
                         "until" : "2017-08-27",
                         "childcareCost" : 30.0,
                         "disability": {
                           "disabled": false,
                           "severelyDisabled": false
                         }
                       }
                     ]
                   }
                 ]
               }
             }
           }
         }
        """.stripMargin)

    val result = Json.toJson[CalculatorInput](tfcCalculator)
    result shouldBe outputJson

  }
}
