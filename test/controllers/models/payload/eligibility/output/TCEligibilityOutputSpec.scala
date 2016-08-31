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

package controllers.models.payload.eligibility.output

import models.payload.eligibility.output.EligibilityOutput
import models.payload.eligibility.output.tc._
import org.joda.time.LocalDate
import mappings.Periods
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by user on 18/03/16.
  */
class TCEligibilityOutputSpec extends UnitSpec {

  "read a valid TC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
         {
            "eligibility": {
              "tc": {
                "eligible": true,
                "taxYears": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "houseHoldIncome": 0.00,
                    "periods": [
                      {
                        "from": "2016-08-27",
                        "until": "2017-04-06",
                        "householdElements": {
                          "basic": false,
                          "hours30": false,
                          "childcare": false,
                          "loneParent": false,
                          "secondParent": false,
                          "family": false
                        },
                        "claimants": [
                          {
                            "qualifying": true,
                            "isPartner": false,
                            "claimantDisability": {
                              "disability": false,
                              "severeDisability": false
                            },
                            "failures": [
                            ]
                          }
                        ],
                        "children": [
                          {
                            "id": 0,
                            "name": "Child 1",
                            "childcareCost": 3000.00,
                            "childcareCostPeriod": "Month",
                            "qualifying": false,
                            "childElements":
                            {
                              "child": false,
                              "youngAdult": false,
                              "disability": false,
                              "severeDisability": false,
                              "childcare": false
                            },
                            "failures": []
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
       """.stripMargin
    )

    val result = json.validate[EligibilityOutput]

    result match {
      case JsSuccess(x, _) => {
        x shouldBe a[EligibilityOutput]
        x.eligibility should not be null

        x.eligibility.tc.isInstanceOf[Option[TCEligibilityOutput]] shouldBe true

        //TaxYear model
        x.eligibility.tc.head.taxYears.isInstanceOf[List[TaxYear]] shouldBe true
        x.eligibility.tc.head.taxYears.head.from shouldBe a[LocalDate]
        x.eligibility.tc.head.taxYears.head.until shouldBe a[LocalDate]
        x.eligibility.tc.head.taxYears.head.houseHoldIncome.isInstanceOf[BigDecimal] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.isInstanceOf[List[TCPeriod]] shouldBe true

        //TCPeriod model
        x.eligibility.tc.head.taxYears.head.periods.head.from shouldBe a[LocalDate]
        x.eligibility.tc.head.taxYears.head.periods.head.until shouldBe a[LocalDate]
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements shouldBe a[HouseholdElements]
        x.eligibility.tc.head.taxYears.head.periods.head.children.isInstanceOf[List[TCOutputChild]] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.isInstanceOf[List[TCOutputClaimant]] shouldBe true

        //HouseholdElements model
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.basic.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.childcare.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.family.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.hours30.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.loneParent.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.householdElements.secondParent.isInstanceOf[Boolean] shouldBe true

        //Child model
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childcareCost.isInstanceOf[BigDecimal] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childcareCostPeriod shouldBe a[Periods.Period]
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements shouldBe a[ChildElements]
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.failures.isInstanceOf[List[String]] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.id.isInstanceOf[Short] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.name.isInstanceOf[Option[String]] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.qualifying.isInstanceOf[Boolean] shouldBe true

        //ChildElements model
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements.child.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements.childcare.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements.disability.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements.severeDisability.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.children.head.childElements.youngAdult.isInstanceOf[Boolean] shouldBe true

        //Claimant model
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.qualifying.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.isPartner.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.failures.isInstanceOf[List[String]] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.claimantDisability shouldBe a[ClaimantDisability]

        //claimantDisability model
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.claimantDisability.disability.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tc.head.taxYears.head.periods.head.claimants.head.claimantDisability.severeDisability.isInstanceOf[Boolean] shouldBe true

      }
      case _ => throw new Exception

    }

  }
 }
