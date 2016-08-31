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

package service

import connectors.{CalculatorConnector, EligibilityConnector}
import controllers.FakeCCApplication
import controllers.keystore.CCSession
import models.payload.calculator.input.CalculatorInput
import models.payload.calculator.output.{Calculation, CalculatorOutput}
import models.payload.eligibility.input.esc.ESCPayload
import models.payload.eligibility.input.tc.TCPayload
import models.payload.eligibility.input.tfc.TFCPayload
import models.payload.eligibility.output.{OutputEligibility, EligibilityOutput}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 24/03/16.
 */
class ResultServiceSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockResultService = new ResultService with CCSession {
    override val eligibilityConnector = mock[EligibilityConnector]
    override val payLoadEligibilityService = mock[PayloadEligibilityService]
    override val calculatorConnector = mock[CalculatorConnector]
    override val payLoadCalculatorService = mock[PayloadCalculatorService]
  }


  implicit val hc = HeaderCarrier()

  "ResultService" when {

    "use the correct eligibility connector" in {
      ResultService.eligibilityConnector shouldBe EligibilityConnector
    }

    "use the correct calculator connector" in {
      ResultService.calculatorConnector shouldBe CalculatorConnector
    }

    "getEligibilityResult" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)

      val children = List(
        _root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(child1Dob),
          childCareCost = Some(BigDecimal(600.00)),
          education = None,
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          )
        )
      )

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = _root_.models.claimant.Disability(
          disabled = false,
          severelyDisabled = true,
          incomeBenefits = true,
          carersAllowance = true,
          noBenefits = true
        ),
        previousIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = Some(BigDecimal(200.00)),
          otherIncome = Some(BigDecimal(1000.00)),
          benefits = Some(BigDecimal(1000.00))
        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = None,
          pension = Some(BigDecimal(100.00)),
          otherIncome = Some(BigDecimal(2000.00)),
          benefits = Some(BigDecimal(3000.00))
        )),
        hours = Some(37.5)
      ))

      val tfcPayload = mock[TFCPayload]
      when(mockResultService.payLoadEligibilityService.createTFCEligibilityPayload(mockEq(claimants), mockEq(children))).thenReturn(tfcPayload)

      val tcPayload = mock[TCPayload]
      when(mockResultService.payLoadEligibilityService.createTCEligibilityPayload(mockEq(claimants), mockEq(children))).thenReturn(tcPayload)

      val escPayload = mock[ESCPayload]
      when(mockResultService.payLoadEligibilityService.createESCEligibilityPayload(mockEq(claimants), mockEq(children))).thenReturn(escPayload)

     val eligibilityOutput = getEligibilityOutput()

     val tfcEligibilityOutput = EligibilityOutput(OutputEligibility(tfc = eligibilityOutput.eligibility.tfc, tc = None, esc = None))
     val tcEligibilityOutput = EligibilityOutput(OutputEligibility(tc = eligibilityOutput.eligibility.tc, tfc = None, esc = None))
     val escEligibilityOutput = EligibilityOutput(OutputEligibility(esc = eligibilityOutput.eligibility.esc, tfc = None, tc = None))

      when(mockResultService.eligibilityConnector.getTFCEligibility(mockEq(tfcPayload))(any())).thenReturn(tfcEligibilityOutput)
      when(mockResultService.eligibilityConnector.getESCEligibility(mockEq(escPayload))(any())).thenReturn(escEligibilityOutput)
      when(mockResultService.eligibilityConnector.getTCEligibility(mockEq(tcPayload))(any())).thenReturn(tcEligibilityOutput)

      val result = await((mockResultService.getEligibilityResult(claimants, children)(hc)))

      result shouldBe eligibilityOutput
    }

    "getCalculatorResult" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)

      val children = List(
        _root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(child1Dob),
          childCareCost = Some(BigDecimal(600.00)),
          education = None,
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          )
        )
      )

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = _root_.models.claimant.Disability(
          disabled = false,
          severelyDisabled = true,
          incomeBenefits = true,
          carersAllowance = true,
          noBenefits = true
        ),
        previousIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = Some(BigDecimal(200.00)),
          otherIncome = Some(BigDecimal(1000.00)),
          benefits = Some(BigDecimal(1000.00))
        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = None,
          pension = Some(BigDecimal(100.00)),
          otherIncome = Some(BigDecimal(2000.00)),
          benefits = Some(BigDecimal(3000.00))
        )),
        hours = Some(37.5)
      ))

      val eligibilityOutput = getEligibilityOutput()

      val calculatorInput = mock[CalculatorInput]

      when(mockResultService.payLoadCalculatorService.getTFCCalculatorPayload(mockEq(eligibilityOutput), mockEq(children))).thenReturn(calculatorInput)

      when(mockResultService.payLoadCalculatorService.getTCCalculatorPayload(mockEq(eligibilityOutput), mockEq(claimants))).thenReturn(calculatorInput)

      when(mockResultService.payLoadCalculatorService.getESCCalculatorPayload(mockEq(eligibilityOutput), mockEq(children), mockEq(claimants))).thenReturn(calculatorInput)

      val calculatorOutput = getCalculatorOutput()

      val calculatorTCOutput = CalculatorOutput(Calculation(tc = calculatorOutput.calculation.tc, tfc = None, esc = None))

      val calculatorTFCOutput = CalculatorOutput(Calculation(tfc = calculatorOutput.calculation.tfc, tc = None, esc = None))

      val calculatorESCOutput = CalculatorOutput(Calculation(esc = calculatorOutput.calculation.esc, tc = None, tfc = None))

      when(mockResultService.calculatorConnector.getTFCCalculatorResult(mockEq(calculatorInput))(any())).thenReturn(calculatorTFCOutput)

      when(mockResultService.calculatorConnector.getESCCalculatorResult(mockEq(calculatorInput))(any())).thenReturn(calculatorESCOutput)

      when(mockResultService.calculatorConnector.getTCCalculatorResult(mockEq(calculatorInput))(any())).thenReturn(calculatorTCOutput)

      val result = await((mockResultService.getCalculatorResult(eligibilityOutput, claimants, children)(hc)))

      result shouldBe calculatorOutput
    }
  }

  private def getEligibilityOutput() =  {
    val outputJson = Json.parse(
      s"""
      {
        "eligibility": {
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
                        "qualifying": false,
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
                        "id": 0,
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
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
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
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
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
        """.stripMargin)
    val output = outputJson.validate[EligibilityOutput]
    output.get
  }

  private def getCalculatorOutput() =  {
    val outputJson = Json.parse(
      s"""
      {
        "calculation" : {
          "tfc": {
            "from": "2016-08-27",
            "until": "2016-11-27",
            "householdContribution": {
              "parent": 6500.00,
              "government": 1000.00,
              "totalChildCareSpend": 7500.00
            },
            "numberOfPeriods" : 1,
            "periods" : [
              {
                "from": "2016-08-27",
                "until": "2016-11-27",
                "periodContribution": {
                  "parent": 6500.00,
                  "government": 1000.00,
                  "totalChildCareSpend": 7500.00
                },
                "children": [
                  {
                    "id": 0,
                    "name" : "Child 1",
                    "childCareCost": 2500.00,
                    "childContribution" : {
                      "parent": 6500.00,
                      "government": 1000.00,
                      "totalChildCareSpend": 7500.00
                    },
                    "timeToMaximizeTopUp" : 0,
                    "failures" : []
                  }
                ]
              }
            ]
          },
         "esc": {
                "from": "2016-08-27",
                "until": "2017-04-06",
                "totalSavings": {
                  "totalSaving": 0,
                  "taxSaving": 0,
                  "niSaving": 0
                },
                "taxYears": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "totalSavings": {
                      "totalSaving": 0,
                      "taxSaving": 0,
                      "niSaving": 0
                    },
                    "claimants": [
                      {
                        "qualifying": false,
                        "eligibleMonthsInTaxYear": 0,
                        "isPartner": false,
                        "escAmount": 200,
                        "escAmountPeriod": "Month",
                        "escStartDate": "2013-08-27",
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
                          "totalSaving": 0,
                          "taxSaving": 0,
                          "niSaving": 0
                        },
                        "taxAndNIBeforeSacrifice": {
                          "taxPaid": 766.6,
                          "niPaid": 361.0
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
           }
        }
      }
        """.stripMargin)
    val output = outputJson.validate[CalculatorOutput]
    output.get
  }

}
