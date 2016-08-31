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

package controllers.models.payload.eligibility.input

import models.payload.eligibility.input._
import models.payload.eligibility.input.tfc.{TFCPayload, TFCEligibility, TFC}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import mappings.Periods

/**
 * Created by user on 10/03/16.
 */
class TFCEligibilitySpec extends UnitSpec{

  "Return a valid request for TFC eligibility" in {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dateOfBirth = LocalDate.parse("2014-05-12", formatter)
    val from = LocalDate.parse("2016-08-27", formatter)

    val claimantList = List(
      Claimant(
        hoursPerWeek = 30,
        liveOrWork = true,
        isPartner = false,
        totalIncome  = BigDecimal(0.00),
        earnedIncome = BigDecimal(0.00),
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        schemesClaiming = SchemesClaiming(
          esc = false,
          tfc = false,
          tc = false,
          uc = false,
          cg = false
        ),
        previousTotalIncome = BigDecimal(0.00),
        employerProvidesESC = false,
        elements = ClaimantsElements(
          vouchers  = false
       ),
        otherSupport = OtherSupport(
          disabilityBenefitsOrAllowances = false,
          severeDisabilityBenefitsOrAllowances = false,
          incomeBenefitsOrAllowances = false,
          carersAllowance = false
          )
      )
    )

    val childList = List(Child(
      id = 1,
      name = Some("Child 1"),
      childcareCost = BigDecimal(3000.00),
      childcareCostPeriod = Periods.Monthly,
      dob = dateOfBirth,
      disability = Disability(
        disabled = false,
        severelyDisabled = false,
        incapacitated = false
      ),
      education = Some(Education(
        inEducation = true,
        startDate = null
      ))
    ))

    val tfcObject = TFC(
      from = from,
      numberOfPeriods = 4,
      claimants = claimantList,
      children = childList
    )

    val tfcPayload = TFCPayload(eligibility = TFCEligibility(tfc = tfcObject))


    val outputJson = Json.parse(
      s"""
         {

            "payload": {
              "tfc": {
                "from": "2016-08-27",
                "numberOfPeriods": 4,
                "claimants": [
                  {
                    "hoursPerWeek": 30.0,
                    "liveOrWork": true,
                    "isPartner": false,
                    "totalIncome": 0.0,
                    "earnedIncome": 0.0,
                    "disability": {
                      "disabled": false,
                      "severelyDisabled": false,
                      "incapacitated": false
                    },
                    "schemesClaiming": {
                      "esc": false,
                      "tfc": false,
                      "tc": false,
                      "uc": false,
                      "cg": false
                    },
                     "previousTotalIncome": 0.0,
                     "employerProvidesESC": false,
                     "elements": {
                        "vouchers": false
                     },
                    "otherSupport": {
                      "disabilityBenefitsOrAllowances":false,
                      "severeDisabilityBenefitsOrAllowances":false,
                      "incomeBenefitsOrAllowances":false,
                      "carersAllowance":false
                    }
                  }
                ],
                "children": [
                  {
                    "id": 1,
                    "name": "Child 1",
                    "childcareCost": 3000.0,
                    "childcareCostPeriod": "Month",
                    "dob": "2014-05-12",
                    "disability": {
                      "disabled": false,
                      "severelyDisabled": false,
                      "incapacitated": false
                    },
                    "education": {
                     "inEducation" : true,
                     "startDate" : null
                    }
                  }
                ]
              }
            }
          }
        """.stripMargin)


    val result = Json.toJson[TFCPayload](tfcPayload)
    result shouldBe outputJson
  }
}
