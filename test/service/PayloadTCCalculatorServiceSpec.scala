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
import models.payload.eligibility.output.esc
import models.payload.eligibility.output.tc
import models.payload.eligibility.output.tfc
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import models.payload.eligibility.output._
import models.payload.eligibility.output.tc._
import models.payload.calculator.input.tc.{HouseHoldElements, TCEligibility}

/**
 * Created by user on 24/03/16.
 */
class PayloadTCCalculatorServiceSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "PayloadTCCalculatorService" should {

    "return TC Calculator payload" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val period1Start = LocalDate.parse("2015-08-20", formatter)
      val period1End = LocalDate.parse("2015-04-06", formatter)
      val period2Start = LocalDate.parse("2015-04-06", formatter)
      val period2End = LocalDate.parse("2017-04-06", formatter)
      val proRataEnd = LocalDate.parse("2016-08-20", formatter)

      // Calculator Output Details
      val householdElements = HouseholdElements(
        basic = true,
        hours30 =  false,
        childcare =  true,
        loneParent =  true,
        secondParent =  false,
        family =  true
      )

      val claimants = List(
        TCOutputClaimant(
          qualifying =  true,
          isPartner =  false,
          claimantDisability =  ClaimantDisability(
            disability =  false,
            severeDisability =  false
          ),
          failures =  List("hours30")
        ),
        TCOutputClaimant(
          qualifying =  true,
          isPartner =  true,
          claimantDisability =  ClaimantDisability(
            disability =  false,
            severeDisability =  false
          ),
          failures =  List("hours30")
          )
      )

      val children =  List(
        TCOutputChild(
          id =  0,
          name =  Some("Child 1"),
          qualifying =  true,
          childcareCost =  200.00,
          childcareCostPeriod =  Periods.Monthly,
          childElements =  ChildElements(
            child =  true,
            youngAdult =  false,
            disability =  false,
            severeDisability =  false,
            childcare =  true
            ),
          failures = List()
        )
      )

      val tcPeriod1 = List(
        TCPeriod(
          from = period1Start,
          until = period1End,
          householdElements = householdElements,
          children = children,
          claimants = claimants
        )
      )

      val tcPeriod2 = List(
        TCPeriod(
          from = period2Start,
          until = period2End,
          householdElements = householdElements,
          children = children,
          claimants = claimants
        )
      )

      val tcOutputTaxYears = List(
        TaxYear(
          from = period1Start,
          until = period1End,
          houseHoldIncome = BigDecimal(34645.00),
          periods = tcPeriod1
        ),TaxYear(
          from = period2Start,
          until = period2End,
          houseHoldIncome = BigDecimal(34645.00),
          periods = tcPeriod2
        )
      )

      val tcEligibilityOutput = TCEligibilityOutput(eligible = true, taxYears = tcOutputTaxYears)

      val outputEligibility = OutputEligibility(tc = Some(tcEligibilityOutput))

      val tcClaimants = List(
        _root_.models.claimant.Claimant(
          id = 1, // default to 1 for parent and 2 for partner
          disability = Disability(false, false, false, false, false),
          previousIncome = None,
          currentIncome = None,
          hours = Some(14)
        ),
        _root_.models.claimant.Claimant(
          id = 2, // default to 1 for parent and 2 for partner
          disability = Disability(false, false, false, false, false),
          previousIncome = None,
          currentIncome = None,
          hours = Some(12)
        )
      )

      val eligibilityOutput = EligibilityOutput(eligibility = outputEligibility)

      // Calculator Input Details
      val inputChildren = List(
        models.payload.calculator.input.tc.Child(
          id = 0,
          name = "Child 1",
          qualifying = true,
          childcareCost = 200,
          childcareCostPeriod = Periods.Monthly,
          childElements = models.payload.calculator.input.tc.ChildElements(
            child = true,
            youngAdult = false,
            disability = false,
            severeDisability = false,
            childcare = true
          )
        )
      )
      val inputClaimant = List(
        models.payload.calculator.input.tc.Claimant(
          qualifying = true,
          isPartner = false,
          claimantElements = models.payload.calculator.input.tc.ClaimantDisability(false, false),
          doesNotTaper = false,
          failures = Some(List())
        ),
        models.payload.calculator.input.tc.Claimant(
          qualifying = true,
          isPartner = true,
          claimantElements = models.payload.calculator.input.tc.ClaimantDisability(false, false),
          doesNotTaper = false,
          failures = Some(List())
        )
      )

      val inputHouseHoldElements = HouseHoldElements(true, false, true, true, false, true)

      val inputPeriod1 = List(
        models.payload.calculator.input.tc.Period(
          from = period1Start,
          until = period1End,
          householdElements = inputHouseHoldElements,
          claimants = inputClaimant,
          children = inputChildren
        )
      )

      val inputPeriod2 = List(
        models.payload.calculator.input.tc.Period(
          from = period2Start,
          until = period2End,
          householdElements = inputHouseHoldElements,
          claimants = inputClaimant,
          children = inputChildren
        )
      )

      val tcInputTaxYears = List(
        models.payload.calculator.input.tc.TaxYear(
          from = period1Start,
          until = period1End,
          houseHoldIncome = BigDecimal(34645.00),
          periods = inputPeriod1
        ),
        models.payload.calculator.input.tc.TaxYear(
          from = period2Start,
          until = period2End,
          houseHoldIncome = BigDecimal(34645.00),
          periods = inputPeriod2
        )
      )

      val outputTCCalculator = TCEligibility(proRataEnd = proRataEnd, taxYears = tcInputTaxYears)
      val eligibility = Eligibility(tc = Some(outputTCCalculator), esc = None, tfc = None)
      val calculatorInput = CalculatorInput(CalculatorPayload(eligibility))

      PayloadCalculatorService.getTCCalculatorPayload(eligibilityOutput, claimants = tcClaimants) shouldBe calculatorInput
    }
  }
  
}
