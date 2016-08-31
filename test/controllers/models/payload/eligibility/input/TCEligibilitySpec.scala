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

import models.payload.eligibility.input.tc.{TCPayload, TCEligibility}

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import mappings.Periods

/**
 * Created by user on 10/03/16.
 */
class TCEligibilitySpec extends UnitSpec{

  "Return a valid request for TC eligibility" in {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dateOfBirth = LocalDate.parse("2010-05-31", formatter)
    val from = LocalDate.parse("2016-04-06", formatter)
    val until = LocalDate.parse("2017-04-06", formatter)

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
      id = 0,
      name = Some("Child 1"),
      childcareCost = BigDecimal(2000.00),
      childcareCostPeriod = Periods.Monthly,
      dob = dateOfBirth,
      disability = Disability(
        disabled = false,
        severelyDisabled = false,
        incapacitated = false
      ),
      education = Some(Education(
        inEducation = true,
        startDate = from
      ))
    ))

    val taxYear =  TaxYear(
      from = from,
      until = until,
      claimants = claimantList,
      children = childList
    )

    val tcPayLoad = TCPayload(eligibility = TCEligibility(List(taxYear)))


    val outputJson = Json.parse(
      s"""
         {

            "payload": {
              "taxYears": [
              {
                "from": "2016-04-06",
                "until": "2017-04-06",
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
                    "id": 0,
                    "name": "Child 1",
                    "childcareCost": 2000.0,
                    "childcareCostPeriod": "Month",
                    "dob": "2010-05-31",
                    "disability": {
                      "disabled": false,
                      "severelyDisabled": false,
                      "incapacitated": false
                    },
                    "education": {
                     "inEducation" : true,
                     "startDate" : "2016-04-06"
                    }
                  }
                ]
              }
            ]
            }
          }
        """.stripMargin)


    val result = Json.toJson[TCPayload](tcPayLoad)
    result shouldBe outputJson
  }

}
