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

package connectors


import config. WSHttp
import controllers.FakeCCApplication
import controllers.keystore.CCSession
import models.payload.eligibility.input.esc.ESCPayload
import models.payload.eligibility.input.tc.TCPayload
import models.payload.eligibility.input.tfc.TFCPayload
import models.payload.eligibility.output.EligibilityOutput
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{HeaderCarrier}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


/**
 * Created by user on 22/03/16.
 */
class EligibilityConnectorSpec extends UnitSpec  with FakeCCApplication with MockitoSugar  {

  val mockHttp = mock[WSHttp]

  val mockConnector = new EligibilityConnector with CCSession {
    override def httpPost  = mockHttp
  }

  implicit val hc = HeaderCarrier()

  "Eligibility Connector" should {

    "get TFCEligibility Result" in {

      val outputJson = Json.parse(
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
       """.stripMargin)

      val output = outputJson.validate[EligibilityOutput]
      val tfcPayload = mock[TFCPayload]

      when(mockConnector.httpPost.POST[TFCPayload, EligibilityOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getTFCEligibility(tfcPayload), 10 seconds)

      result shouldBe outputJson.as[EligibilityOutput]

    }

    "get TCEligibility Result" in {

      val outputJson = Json.parse(
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

      val output = outputJson.validate[EligibilityOutput]

      val tcPayload = mock[TCPayload]

      when(mockConnector.httpPost.POST[TFCPayload, EligibilityOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getTCEligibility(tcPayload), 10 seconds)

      result shouldBe outputJson.as[EligibilityOutput]

    }

    "get EsEligibility Result" in {

      val outputJson = Json.parse(
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

      val output = outputJson.validate[EligibilityOutput]

      val escPayload = mock[ESCPayload]

      when(mockConnector.httpPost.POST[TFCPayload, EligibilityOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getESCEligibility(escPayload), 10 seconds)

      result shouldBe outputJson.as[EligibilityOutput]

    }
  }
}
