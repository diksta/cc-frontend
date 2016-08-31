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

import controllers.FakeCCApplication
import models.payload.calculator.input.{CalculatorInput, CalculatorPayload, Eligibility}
import models.payload.calculator.input.tfc.TFCEligibility
import models.payload.eligibility.output._
import models.payload.eligibility.output.tfc.TFCEligibilityOutput
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
* Created by user on 24/03/16.
*/
class PayloadTFCCalculatorServiceSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "PayloadTFCCalculatorService" should {

    "return TFC Calculator payload" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val period1Start = LocalDate.parse("2000-08-20", formatter)
      val period1End = LocalDate.parse("2000-11-20", formatter)

      // Calculator Output Details
      val claimants = List(
        models.payload.eligibility.output.tfc.TFCOutputClaimant(
          qualifying =  true,
          isPartner =  false,
          failures =  List()
        ),
        models.payload.eligibility.output.tfc.TFCOutputClaimant(
          qualifying =  true,
          isPartner =  true,
          failures =  List()
        )
      )

      val children =  List(
        models.payload.eligibility.output.tfc.TFCOutputChild(
          id =  1,
          name =  Some("Child 1"),
          qualifying =  true,
          from =  Some(period1Start),
          until =  Some(period1End),
          failures = List()
        ),
        models.payload.eligibility.output.tfc.TFCOutputChild(
          id =  1,
          name =  Some("Child 2"),
          qualifying =  false,
          from =  null,
          until =  null,
          failures = List()
        )
      )

      val tfcPeriods = List(
        models.payload.eligibility.output.tfc.TFCPeriod(
          from = period1Start,
          until = period1End,
          periodEligibility = true,
          children = children,
          claimants = claimants
        )
      )

      val tfcEligibilityOutput = TFCEligibilityOutput(
        from = period1Start,
        until = period1End,
        householdEligibility = true,
        periods = tfcPeriods
      )

      val outputEligibility = OutputEligibility(tfc = Some(tfcEligibilityOutput))

      val eligibilityOutput = EligibilityOutput(eligibility = outputEligibility)

      // Calculator Input Details
      val inputChildren = List(
        models.payload.calculator.input.Child(
          id = 1,
          name = Some("Child 1"),
          qualifying = true,
          childcareCost = 300,
          from = period1Start,
          until = period1End,
          disability = models.payload.calculator.input.Disability(disabled = false, severelyDisabled = false)
        ),
        models.payload.calculator.input.Child(
          id = 1,
          name = Some("Child 2"),
          qualifying = false,
          childcareCost = 300,
          from = null,
          until = null,
          disability = models.payload.calculator.input.Disability(disabled = false, severelyDisabled = false)
        )
      )

      val tfcInputPeriods = List(
        models.payload.calculator.input.tfc.TFCPeriod(
          from = period1Start,
          until = period1End,
          periodEligibility = true,
          children = inputChildren
        )
      )

      val tfcInputChildren = List(
        _root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = None,
          disability = models.child.Disability(disabled = false, severelyDisabled = false, blind = false, nonDisabled = true),
          childCareCost = Some(300)
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = None,
          disability = models.child.Disability(disabled = false, severelyDisabled = false, blind = false, nonDisabled = true),
          childCareCost = Some(0)
        )
      )

      val outputTFCCalculator = TFCEligibility(from = period1Start, until = period1End, householdEligibility = true, periods = tfcInputPeriods)
      val eligibility = Eligibility(tfc = Some(outputTFCCalculator), esc = None, tc = None)
      val calculatorInput = CalculatorInput(CalculatorPayload(eligibility))

      PayloadCalculatorService.getTFCCalculatorPayload(eligibilityOutput = eligibilityOutput, childrenInput = tfcInputChildren) shouldBe calculatorInput
    }

  }

}
