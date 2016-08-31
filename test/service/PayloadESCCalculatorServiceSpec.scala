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
import mappings.Periods
import models.claimant.Disability
import models.payload.calculator.input._
import models.payload.calculator.input.esc.{ESCIncome, ESCEligibility, ESCElements}
import models.payload.eligibility.output._
import models.payload.eligibility.output.esc
import models.payload.eligibility.output.esc._
import models.payload.eligibility.output.tc
import models.payload.eligibility.output.tfc
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 30/03/16.
 */
class PayloadESCCalculatorServiceSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "PayloadESCCalculatorService" should {

    "return ESC Calculator payload with income" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val period1Start = LocalDate.parse("2000-08-20", formatter)
      val period1End = LocalDate.parse("2000-11-20", formatter)

      // Calculator Output Details
      val claimants = List(
        ESCOutputClaimant(
          qualifying =  true,
          isPartner =  false,
          eligibleMonthsInPeriod = 7,
          elements =  ClaimantElements(
            vouchers =  true
          ),
          failures =  List()
        ),
        ESCOutputClaimant(
          qualifying =  true,
          isPartner =  true,
          eligibleMonthsInPeriod = 0,
          elements =  ClaimantElements(
            vouchers =  true
          ),
          failures =  List()
        )
      )

      val children =  List(
        ESCOutputChild(
          id =  1,
          name =  Some("Child 1"),
          qualifying =  true,
          failures = List()
        )
      )

      val escPeriods = List(
        ESCPeriod(
          from = period1Start,
          until = period1End,
          children = children,
          claimants = claimants
        )
      )

      val escOutputTaxYears = List(
        TaxYear(
          from = period1Start,
          until = period1End,
          periods = escPeriods
        )
      )

      val escEligibilityOutput = ESCEligibilityOutput(taxYears = escOutputTaxYears)

      val outputEligibility = OutputEligibility(esc = Some(escEligibilityOutput))

      val eligibilityOutput = EligibilityOutput(eligibility = outputEligibility)

      // Calculator Input Details
      val inputClaimants = List(
        models.payload.calculator.input.esc.ESCClaimant(
          qualifying = true,
          isPartner = false,
          eligibleMonthsInPeriod = 7,
          income = ESCIncome(27600, 30000, "", ""),
          elements = ESCElements(vouchers = true),
          escStartDate = LocalDate.now(),
          escAmount = 250,
          escAmountPeriod = Periods.Monthly
        ),
        models.payload.calculator.input.esc.ESCClaimant(
          qualifying = true,
          isPartner = true,
          eligibleMonthsInPeriod = 0,
          income = ESCIncome(28800, 30000, "", ""),
          elements = ESCElements(vouchers = true),
          escStartDate = LocalDate.now(),
          escAmount = 250,
          escAmountPeriod = Periods.Monthly
        )
      )

      val inputPeriod = List(
        models.payload.calculator.input.esc.ESCPeriod(
          from = period1Start,
          until = period1End,
          claimants = inputClaimants
        )
      )

      val escInputTaxYears = List(
        models.payload.calculator.input.esc.ESCTaxYear(
          startDate = period1Start,
          endDate = period1End,
          periods = inputPeriod
        )
      )

      // user model inputs
      val escClaimants = List(
        _root_.models.claimant.Claimant(
          id = 1, // default to 1 for parent and 2 for partner
          disability = Disability(false, false, false, false, false),
          previousIncome = Some(models.claimant.Income(Some(7000), Some(200), None, None)),
          currentIncome = Some(models.claimant.Income(Some(30000), None, None, None)),
          hours = Some(14)
        ),
        _root_.models.claimant.Claimant(
          id = 2, // default to 1 for parent and 2 for partner
          disability = Disability(false, false, false, false, false),
          previousIncome = Some(models.claimant.Income(Some(30000), None, None, None)),
          currentIncome = Some(models.claimant.Income(None, Some(100), None, None)),
          hours = Some(12)
        )
      )

      val escInputChildren = List(
        _root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = None,
          disability = models.child.Disability(disabled = false, severelyDisabled = false, blind = false, nonDisabled = true),
          childCareCost = Some(500)
        )
      )

      val outputESCCalculator = ESCEligibility(escTaxYear = escInputTaxYears)
      val eligibility = Eligibility(esc = Some(outputESCCalculator), tc = None, tfc = None)
      val calculatorInput = CalculatorInput(CalculatorPayload(eligibility))

      PayloadCalculatorService.getESCCalculatorPayload(eligibilityOutput, childrenInput = escInputChildren, claimants = escClaimants) shouldBe calculatorInput
    }

    "return ESC Calculator payload with no current income" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val period1Start = LocalDate.parse("2000-08-20", formatter)
      val period1End = LocalDate.parse("2000-11-20", formatter)

      // Calculator Output Details
      val claimants = List(
        ESCOutputClaimant(
          qualifying =  false,
          isPartner =  false,
          eligibleMonthsInPeriod = 0,
          elements =  ClaimantElements(
            vouchers =  true
          ),
          failures =  List()
        )
      )

      val children =  List(
        ESCOutputChild(
          id =  1,
          name =  Some("Child 1"),
          qualifying =  false,
          failures = List()
        )
      )

      val escPeriods = List(
        ESCPeriod(
          from = LocalDate.now(),
          until = LocalDate.now(),
          children = children,
          claimants = claimants
        )
      )

      val escOutputTaxYears = List(
        TaxYear(
          from = LocalDate.now(),
          until = LocalDate.now(),
          periods = escPeriods
        )
      )

      val escEligibilityOutput = ESCEligibilityOutput(taxYears = escOutputTaxYears)

      val outputEligibility = OutputEligibility(esc = Some(escEligibilityOutput))

      val eligibilityOutput = EligibilityOutput(eligibility = outputEligibility)

      // Calculator Input Details
      val inputClaimants = List(
        models.payload.calculator.input.esc.ESCClaimant(
          qualifying = false,
          isPartner = false,
          eligibleMonthsInPeriod = 0,
          income = ESCIncome(0.00, 0.00, "", ""),
          elements = ESCElements(vouchers = true),
          escStartDate = LocalDate.now(),
          escAmount = 0,
          escAmountPeriod = Periods.Monthly
        )
      )

      val inputPeriod = List(
        models.payload.calculator.input.esc.ESCPeriod(
          from = LocalDate.now(),
          until = LocalDate.now(),
          claimants = inputClaimants
        )
      )

      val escInputTaxYears = List(
        models.payload.calculator.input.esc.ESCTaxYear(
          startDate = LocalDate.now(),
          endDate = LocalDate.now(),
          periods = inputPeriod
        )
      )

      // user model inputs
      val escClaimants = List(
        _root_.models.claimant.Claimant(
          id = 1, // default to 1 for parent and 2 for partner
          disability = Disability(false, false, false, false, false),
          previousIncome = None,
          currentIncome = None,
          hours = Some(12)
        )
      )

      val escInputChildren = List(
        _root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = None,
          disability = models.child.Disability(disabled = false, severelyDisabled = false, blind = false, nonDisabled = true),
          childCareCost = Some(0)
        )
      )

      val outputESCCalculator = ESCEligibility(escTaxYear = escInputTaxYears)
      val eligibility = Eligibility(esc = Some(outputESCCalculator), tc = None, tfc = None)
      val calculatorInput = CalculatorInput(CalculatorPayload(eligibility))

      PayloadCalculatorService.getESCCalculatorPayload(eligibilityOutput, childrenInput = escInputChildren, claimants = escClaimants) shouldBe calculatorInput
    }
  }

}
