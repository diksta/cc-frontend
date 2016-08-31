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
import models.payload.eligibility.output.esc._
import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 18/03/16.
 */
class ESCEligibilityOutputSpec extends UnitSpec {

  "read a valid ESC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
         {
            "eligibility": {
              "tc": null,
              "esc": {
                "taxYears": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "periods": [
                      {
                        "from": "2016-08-27",
                        "until": "2017-04-06",
                        "claimants": [
                          {
                            "qualifying": true,
                            "isPartner": false,
                            "eligibleMonthsInPeriod": 0,
                            "elements": {
                              "vouchers": false
                            },
                            "failures": []
                          }
                        ],
                        "children": [
                          {
                            "id": 1,
                            "name": "Child 1",
                            "qualifying": true,
                            "failures": []
                          }
                        ]
                      }
                    ]
                  }
                ]
              },
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

        x.eligibility.esc.isInstanceOf[Option[ESCEligibilityOutput]] shouldBe true

        //TaxYear model
        x.eligibility.esc.head.taxYears.isInstanceOf[List[TaxYear]] shouldBe true
        x.eligibility.esc.head.taxYears.head.from shouldBe a[LocalDate]
        x.eligibility.esc.head.taxYears.head.until shouldBe a[LocalDate]
        x.eligibility.esc.head.taxYears.head.periods.isInstanceOf[List[ESCPeriod]] shouldBe true

        //ESC Period model
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.isInstanceOf[List[ESCOutputClaimant]] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.children.isInstanceOf[List[ESCOutputChild]] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.from shouldBe a[LocalDate]
        x.eligibility.esc.head.taxYears.head.periods.head.until shouldBe a[LocalDate]

        //Claimants model
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.elements shouldBe a[ClaimantElements]
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.eligibleMonthsInPeriod.isInstanceOf[Int] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.failures.isInstanceOf[List[String]] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.isPartner.isInstanceOf[Boolean] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.qualifying.isInstanceOf[Boolean] shouldBe true

        //Claimant Elements
        x.eligibility.esc.head.taxYears.head.periods.head.claimants.head.elements.vouchers.isInstanceOf[Boolean] shouldBe true

        //Children model
        x.eligibility.esc.head.taxYears.head.periods.head.children.head.failures.isInstanceOf[List[String]] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.children.head.id.isInstanceOf[Short] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.children.head.name.isInstanceOf[Option[String]] shouldBe true
        x.eligibility.esc.head.taxYears.head.periods.head.children.head.qualifying.isInstanceOf[Boolean] shouldBe true

      }
      case _ => throw new Exception

    }

  }
}
