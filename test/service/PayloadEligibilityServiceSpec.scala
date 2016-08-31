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
import models.payload.eligibility.input._
import models.payload.eligibility.input.esc.{ESCPayload, ESCEligibility}
import models.payload.eligibility.input.tc.{TCPayload, TCEligibility}
import models.payload.eligibility.input.tfc.{TFCPayload, TFCEligibility, TFC}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import service.PayloadEligibilityService._
import mappings.Periods

/**
 * Created by user on 17/03/16.
 */
class PayloadEligibilityServiceSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "PayloadEligibilityService" should {

    "return TFC Eligibility payload - previousIncome is None and childcare cost for one child is None" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)
      val child2Dob = LocalDate.parse("2010-05-15", formatter)
      val child3Dob = LocalDate.parse("2012-03-21", formatter)

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
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = Some(child2Dob),
          childCareCost = Some(BigDecimal(300.00)),
          education = Some(_root_.models.child.Education(inEducation = false, startDate = None)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )
        ),
        _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          dob = Some(child3Dob),
          childCareCost = None,
          education = None,
          disability = _root_.models.child.Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
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
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = Some(BigDecimal(200.00)),
          otherIncome = Some(BigDecimal(3000.00)),
          benefits = None
        )),
        hours = Some(37.5),
        escVouchersAvailable = Some("Yes")
      ))

      val outputChildren = List(Child(
        id = 1,
        name = Some("Child 1"),
        childcareCost = BigDecimal(600.00),
        childcareCostPeriod = Periods.Monthly,
        dob = child1Dob,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        education = None
      ),
        Child(
          id = 2,
          name = Some("Child 2"),
          childcareCost = BigDecimal(300.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child2Dob,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          education = None
        ),
        Child(
          id = 3,
          name = Some("Child 3"),
          childcareCost = BigDecimal(0.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child3Dob,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            incapacitated = false
          ),
          education = None
        )
      )

      val outputClamaints = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(10600.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
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
          employerProvidesESC = true,
          elements = ClaimantsElements(
            vouchers  = true
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val outputTFC = TFC(
        from = LocalDate.now(),
        numberOfPeriods = 4,
        claimants = outputClamaints,
        children = outputChildren
      )

      val outputTFCPayload = TFCPayload(eligibility = TFCEligibility(tfc = outputTFC))   

      createTFCEligibilityPayload(claimants,children) shouldBe outputTFCPayload

    }

    "return TFC Eligibility payload" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)
      val child2Dob = LocalDate.parse("2010-05-15", formatter)
      val child3Dob = LocalDate.parse("2012-03-21", formatter)

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
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = Some(child2Dob),
          childCareCost = Some(BigDecimal(300.00)),
          education = Some(_root_.models.child.Education(inEducation = false, startDate = None)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )
        ),
        _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          dob = Some(child3Dob),
          childCareCost = Some(BigDecimal(500.00)),
          education = Some(_root_.models.child.Education(inEducation = true, startDate = Some(LocalDate.now()))),
          disability = _root_.models.child.Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
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
          otherIncome = Some(BigDecimal(3000.00)),
          benefits = None
        )),
        hours = Some(37.5),
        escVouchersAvailable = Some("notSure")
      ))

      val outputChildren = List(Child(
        id = 1,
        name = Some("Child 1"),
        childcareCost = BigDecimal(600.00),
        childcareCostPeriod = Periods.Monthly,
        dob = child1Dob,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        education = None
      ),
        Child(
          id = 2,
          name = Some("Child 2"),
          childcareCost = BigDecimal(300.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child2Dob,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          education = None
        ),
        Child(
          id = 3,
          name = Some("Child 3"),
          childcareCost = BigDecimal(500.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child3Dob,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            incapacitated = false
          ),
          education = Some(Education(
            inEducation = true,
            startDate = LocalDate.now()
          ))
        )
      )

      val outputClamaints = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(10600.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(10600.00),
          employerProvidesESC = true,
          elements = ClaimantsElements(
            vouchers  = true
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val outputTFC = TFC(
        from = LocalDate.now(),
        numberOfPeriods = 4,
        claimants = outputClamaints,
        children = outputChildren
      )

      val outputTFCPayload = TFCPayload(eligibility = TFCEligibility(tfc = outputTFC))

      createTFCEligibilityPayload(claimants,children) shouldBe outputTFCPayload

    }

    "return TC Eligibility payload" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)
      val child2Dob = LocalDate.parse("2010-05-15", formatter)
      val child3Dob = LocalDate.parse("2012-03-21", formatter)

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
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = Some(child2Dob),
          childCareCost = Some(BigDecimal(300.00)),
          education = Some(_root_.models.child.Education(inEducation = false, startDate = None)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )
        ),
        _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          dob = Some(child3Dob),
          childCareCost = Some(BigDecimal(500.00)),
          education = Some(_root_.models.child.Education(inEducation = true, startDate = Some(LocalDate.now()))),
          disability = _root_.models.child.Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
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
          pension = Some(BigDecimal(300.00)),
          otherIncome = Some(BigDecimal(3000.00)),
          benefits = Some(BigDecimal(3000.00))
        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = None,
          otherIncome = None,
          benefits = None
        )),
        hours = Some(37.5),
        escVouchersAvailable = Some("No")
      ))

      val outputChildren = List(Child(
        id = 1,
        name = Some("Child 1"),
        childcareCost = BigDecimal(600.00),
        childcareCostPeriod = Periods.Monthly,
        dob = child1Dob,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        education = None
      ),
        Child(
          id = 2,
          name = Some("Child 2"),
          childcareCost = BigDecimal(300.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child2Dob,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          education = None
        ),
        Child(
          id = 3,
          name = Some("Child 3"),
          childcareCost = BigDecimal(500.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child3Dob,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            incapacitated = false
          ),
          education = Some(Education(
            inEducation = true,
            startDate = LocalDate.now()
          ))
        )
      )

      val outputClaimants = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(45400.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(45400.00),
          employerProvidesESC = false,
          elements = ClaimantsElements(
            vouchers  = false
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val now = LocalDate.now()
      val april6thCurrentYear = determineApril6DateFromNow(now)



      val outputTaxYear =  List(TaxYear(
        from = now,
        until = april6thCurrentYear,
        claimants = outputClaimants,
        children = outputChildren
      ),
      TaxYear(
        from = april6thCurrentYear,
        until = april6thCurrentYear.plusYears(1),
        claimants = outputClaimants,
        children = outputChildren
      ))


      val outputTCPayload = TCPayload(eligibility = TCEligibility(outputTaxYear))

      createTCEligibilityPayload(claimants,children) shouldBe outputTCPayload

    }

    "return TC Eligibility payload with partner" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)
      val child2Dob = LocalDate.parse("2010-05-15", formatter)
      val child3Dob = LocalDate.parse("2012-03-21", formatter)

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
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = Some(child2Dob),
          childCareCost = Some(BigDecimal(300.00)),
          education = Some(_root_.models.child.Education(inEducation = false, startDate = None)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )
        ),
        _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          dob = Some(child3Dob),
          childCareCost = Some(BigDecimal(500.00)),
          education = Some(_root_.models.child.Education(inEducation = true, startDate = Some(LocalDate.now()))),
          disability = _root_.models.child.Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
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
          pension = Some(BigDecimal(300.00)),
          otherIncome = Some(BigDecimal(3000.00)),
          benefits = Some(BigDecimal(3000.00))
        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = None,
          otherIncome = None,
          benefits = None
        )),
        hours = Some(37.5),
        escVouchersAvailable = Some("Yes")
      ),
        _root_.models.claimant.Claimant(
          id = 2,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = true,
            incomeBenefits = true,
            carersAllowance = true,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(3000.00)),
            benefits = Some(BigDecimal(3000.00))
          )),
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = None,
            otherIncome = None,
            benefits = None
          )),
          hours = Some(37.5),
          escVouchersAvailable = Some("Yes")
          ))

      val outputChildren = List(Child(
        id = 1,
        name = Some("Child 1"),
        childcareCost = BigDecimal(600.00),
        childcareCostPeriod = Periods.Monthly,
        dob = child1Dob,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        education = None
      ),
        Child(
          id = 2,
          name = Some("Child 2"),
          childcareCost = BigDecimal(300.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child2Dob,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          education = None
        ),
        Child(
          id = 3,
          name = Some("Child 3"),
          childcareCost = BigDecimal(500.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child3Dob,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            incapacitated = false
          ),
          education = Some(Education(
            inEducation = true,
            startDate = LocalDate.now()
          ))
        )
      )

      val outputClaimants = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(45400.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(45400.00),
          employerProvidesESC = true,
          elements = ClaimantsElements(
            vouchers  = true
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        ),
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = true,
          totalIncome  = BigDecimal(45400.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(45400.00),
          employerProvidesESC = true,
          elements = ClaimantsElements(
            vouchers  = true
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val now = LocalDate.now()
      val april6thCurrentYear = determineApril6DateFromNow(now)



      val outputTaxYear =  List(TaxYear(
        from = now,
        until = april6thCurrentYear,
        claimants = outputClaimants,
        children = outputChildren
      ),
        TaxYear(
          from = april6thCurrentYear,
          until = april6thCurrentYear.plusYears(1),
          claimants = outputClaimants,
          children = outputChildren
        ))


      val outputTCPayload = TCPayload(eligibility = TCEligibility(outputTaxYear))

      createTCEligibilityPayload(claimants,children) shouldBe outputTCPayload

    }

    "return ESC Eligibility payload" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val child1Dob = LocalDate.parse("2000-08-20", formatter)
      val child2Dob = LocalDate.parse("2010-05-15", formatter)
      val child3Dob = LocalDate.parse("2012-03-21", formatter)

      val april6thDate = determineApril6DateFromNow(LocalDate.now())

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
        ),
        _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = Some(child2Dob),
          childCareCost = Some(BigDecimal(300.00)),
          education = Some(_root_.models.child.Education(inEducation = false, startDate = None)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )
        ),
        _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          dob = Some(child3Dob),
          childCareCost = Some(BigDecimal(500.00)),
          education = Some(_root_.models.child.Education(inEducation = true, startDate = Some(LocalDate.now()))),
          disability = _root_.models.child.Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
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
        hours = Some(37.5),
        escVouchersAvailable = Some("No")
      ))

      val outputChildren = List(Child(
        id = 1,
        name = Some("Child 1"),
        childcareCost = BigDecimal(600.00),
        childcareCostPeriod = Periods.Monthly,
        dob = child1Dob,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incapacitated = false
        ),
        education = None
      ),
        Child(
          id = 2,
          name = Some("Child 2"),
          childcareCost = BigDecimal(300.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child2Dob,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          education = None
        ),
        Child(
          id = 3,
          name = Some("Child 3"),
          childcareCost = BigDecimal(500.00),
          childcareCostPeriod = Periods.Monthly,
          dob = child3Dob,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            incapacitated = false
          ),
          education = Some(Education(
            inEducation = true,
            startDate = LocalDate.now()
          ))
        )
      )

      val outputClamaints = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(46800.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(20600.00),
          employerProvidesESC = false,
          elements = ClaimantsElements(
            vouchers  = false
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val outputClamaintsForSecondTaxYear = List(
        Claimant(
          hoursPerWeek = 37.5,
          liveOrWork = true,
          isPartner = false,
          totalIncome  = BigDecimal(46800.00),
          earnedIncome = BigDecimal(0.00),
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            incapacitated = false
          ),
          schemesClaiming = SchemesClaiming(
            esc = false,
            tfc = false,
            tc = false,
            uc = false,
            cg = false
          ),
          previousTotalIncome = BigDecimal(46800.00),
          employerProvidesESC = false,
          elements = ClaimantsElements(
            vouchers  = false
          ),
          otherSupport = OtherSupport(
            disabilityBenefitsOrAllowances = false,
            severeDisabilityBenefitsOrAllowances = true,
            incomeBenefitsOrAllowances = true,
            carersAllowance = true
          )
        )
      )

      val outputTaxyear =  List(TaxYear(
        from = LocalDate.now(),
        until = april6thDate,
        claimants = outputClamaints,
        children = outputChildren
      ),
        TaxYear(
          from = april6thDate,
          until = LocalDate.now().plusYears(1),
          claimants = outputClamaintsForSecondTaxYear,
          children = outputChildren
        ))

      val outputESCPayload = ESCPayload(eligibility = ESCEligibility(outputTaxyear))
      
      createESCEligibilityPayload(claimants,children) shouldBe outputESCPayload

    }
  }

  "PayloadEligibilityServiceHelper" should {

    "get april 6th Date when now date is before 6th april of current year" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val now = LocalDate.parse("2016-02-27T00:00:00", formatter)
      val april6thDate = LocalDate.parse("2016-04-06T00:00:00", formatter)
      val result = determineApril6DateFromNow(now)
      result shouldBe april6thDate
    }

    "get april 6th Date when now date is after 6th april of current year" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val now = LocalDate.parse("2016-07-27", formatter)
      val april6thDate = LocalDate.parse("2017-04-06", formatter)
      val result = determineApril6DateFromNow(now)
      result shouldBe april6thDate
    }


  }

}
