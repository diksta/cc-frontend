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


import config.WSHttp
import controllers.FakeCCApplication
import controllers.keystore.CCSession
import models.payload.calculator.input.CalculatorInput
import models.payload.calculator.output.CalculatorOutput
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/**
 * Created by user on 22/03/16.
 */
class CalculatorConnectorSpec extends UnitSpec  with FakeCCApplication with MockitoSugar  {

  val mockHttp = mock[WSHttp]

  val mockConnector = new CalculatorConnector with CCSession {
    override def httpPost  = mockHttp
  }

  implicit val hc = HeaderCarrier()

  "Calculator Connector" should {

    "get TFCEligibility Result" in {

      val outputJson = Json.parse(
        s"""
        {
          "calculation" : {
            "tfc": {
              "from": "2016-08-27",
              "until": "2016-11-27",
              "householdContribution": {
                "parent": 8500.00,
                "government": 500.00,
                "totalChildCareSpend": 9000.00
              },
              "numberOfPeriods" : 1,
              "periods" : [
                {
                  "from": "2016-08-27",
                  "until": "2016-11-27",
                  "periodContribution": {
                    "parent": 8500.00,
                    "government": 500.00,
                    "totalChildCareSpend": 9000.00
                  },
                  "children": [
                    {
                      "id": 0,
                      "name" : "Child 1",
                      "childCareCost": 3000.00,
                      "childContribution" : {
                        "parent": 8500.00,
                        "government": 500.00,
                        "totalChildCareSpend": 9000.00
                      },
                      "timeToMaximizeTopUp" : 0,
                      "failures" : []
                    }
                  ]
                }
              ]
            },
            "esc": null,
            "tc": null
          }
        }
        """.stripMargin
      )

      val output = outputJson.validate[CalculatorOutput]

      val calculatorInput = mock[CalculatorInput]

      when(mockConnector.httpPost.POST[CalculatorInput, CalculatorOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getTFCCalculatorResult(calculatorInput), 10 seconds)

      result shouldBe outputJson.as[CalculatorOutput]

    }

    "get TCEligibility Result" in {

      val outputJson = Json.parse(
        s"""
          {
          "calculation": {
           "tc": {
             "from": "2016-09-27",
             "until": "2017-04-06",
             "totalAwardAmount": 2989.74,
             "totalAwardProRataAmount" :0.00,
             "houseHoldAdviceAmount": 0.00,
             "totalHouseHoldAdviceProRataAmount" :0.00,
             "taxYears": [
             {
                 "from": "2016-09-27",
                 "until": "2017-04-06",
                 "taxYearAwardAmount": 2989.74,
                 "taxYearAwardProRataAmount" : 0.00,
                 "taxYearAdviceAmount": 0.00,
                 "taxYearAdviceProRataAmount" : 0.00,
                 "periods": [
                   {
                    "from": "2016-09-27",
                    "until": "2016-12-12",
                     "periodNetAmount": 2989.74,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                        "wtcWorkElement": {
                          "netAmount": 92.27,
                          "maximumAmount": 995.60,
                          "taperAmount": 903.33
                        },
                        "wtcChildcareElement": {
                          "netAmount": 704.87,
                          "maximumAmount": 704.87,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 2078.60,
                          "maximumAmount": 2078.60,
                          "taperAmount": 0.00
                        },
                        "ctcFamilyElement": {
                          "netAmount": 114.00,
                          "maximumAmount": 114.00,
                          "taperAmount": 0.00
                        }
                      }
                  },
                  {
                    "from": "2016-12-12",
                    "until": "2017-04-06",
                    "periodNetAmount": 0.00,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                      "wtcWorkElement": {
                          "netAmount": 0.00,
                          "maximumAmount":  872.85,
                          "taperAmount":  872.85
                        },
                        "wtcChildcareElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                          },
                        "ctcFamilyElement": {
                          "maximumAmount": 0.00,
                          "netAmount": 0.00,
                          "taperAmount": 0.00
                        }
                      }
                    }
                 ]
              }
             ]
           },
           "tfc": null,
           "esc": null
          }
          }
          """.stripMargin
      )

      val output = outputJson.validate[CalculatorOutput]

      val calculatorInput = mock[CalculatorInput]

      when(mockConnector.httpPost.POST[CalculatorInput, CalculatorOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getTCCalculatorResult(calculatorInput), 10 seconds)

      result shouldBe outputJson.as[CalculatorOutput]

    }

    "get Esc Calculator Result" in {

      val outputJson = Json.parse(
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

      val output = outputJson.validate[CalculatorOutput]

      val calculatorInput = mock[CalculatorInput]

      when(mockConnector.httpPost.POST[CalculatorInput, CalculatorOutput](anyString(), any(),any())(any(),any(), any())).thenReturn(Future.successful((output.get)))

      val result = Await.result(mockConnector.getESCCalculatorResult(calculatorInput), 10 seconds)

      result shouldBe outputJson.as[CalculatorOutput]

    }
  }
}
