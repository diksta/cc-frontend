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
import models.payload.eligibility.output.tfc.{TFCOutputChild, TFCOutputClaimant, TFCPeriod, TFCEligibilityOutput}
import org.joda.time.LocalDate
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by user on 18/03/16.
  */
class TFCEligibilityOutputSpec extends UnitSpec {

  "read a valid TFC JSON input and convert to a specific type" in {

    val json = Json.parse(
      s"""
         {
            "eligibility": {
              "tc": null,
              "esc": null,
              "tfc": {
                "from": "2016-08-27",
                "until": "2016-11-27",
                "householdEligibility": false,
                "periods": [
                  {
                    "from" : "2016-08-27",
                    "until" : "2016-11-27",
                    "periodEligibility" : false,
                    "claimants" : [
                      {
                        "qualifying" : false,
                        "isPartner" : false,
                        "failures" : []
                      }
                    ],
                    "children" : [
                      {
                        "id" : 0,
                        "name" : "Venky",
                        "qualifying" : true,
                        "from" : "2016-08-27",
                        "until" : "2016-11-27",
                        "failures" : []
                      }
                    ]
                  }
                ]
              }
            }
         }
       """.stripMargin
    )

    val result = json.validate[EligibilityOutput]

    result match {
      case JsSuccess(x, _) => {
        x shouldBe a[EligibilityOutput]
        x.eligibility should not be null

        x.eligibility.tfc.isInstanceOf[Option[TFCEligibilityOutput]] shouldBe true

        //TFC model
        x.eligibility.tfc.head.periods.isInstanceOf[List[TFCPeriod]] shouldBe true
        x.eligibility.tfc.head.from shouldBe a[LocalDate]
        x.eligibility.tfc.head.until shouldBe a[LocalDate]
        x.eligibility.tfc.head.householdEligibility.isInstanceOf[Boolean] shouldBe true

        //TFC Period model
        x.eligibility.tfc.head.periods.head.from shouldBe a[LocalDate]
        x.eligibility.tfc.head.periods.head.until shouldBe a[LocalDate]
        x.eligibility.tfc.head.periods.head.periodEligibility.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tfc.head.periods.head.claimants.isInstanceOf[List[TFCOutputClaimant]] shouldBe true
        x.eligibility.tfc.head.periods.head.children.isInstanceOf[List[TFCOutputChild]] shouldBe true

        //Claimant model
        x.eligibility.tfc.head.periods.head.claimants.head.qualifying.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tfc.head.periods.head.claimants.head.isPartner.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tfc.head.periods.head.claimants.head.failures.isInstanceOf[List[String]] shouldBe true

        //Child model
        x.eligibility.tfc.head.periods.head.children.head.id.isInstanceOf[Short] shouldBe true
        x.eligibility.tfc.head.periods.head.children.head.name.isInstanceOf[Option[String]] shouldBe true
        x.eligibility.tfc.head.periods.head.children.head.qualifying.isInstanceOf[Boolean] shouldBe true
        x.eligibility.tfc.head.periods.head.children.head.failures.isInstanceOf[List[String]] shouldBe true
        x.eligibility.tfc.head.periods.head.children.head.from.isInstanceOf[Option[LocalDate]] shouldBe true
        x.eligibility.tfc.head.periods.head.children.head.until.isInstanceOf[Option[LocalDate]] shouldBe true

      }
      case _ => throw new Exception

    }
  }

 }
