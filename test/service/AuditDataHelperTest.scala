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

import models.claimant.Disability
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 05/08/16.
 */

class AuditDataHelperTest extends UnitSpec {
  val mockAuditDataHelper = new AuditDataHelper{}

  "Audit Data" should {
    "audit data map for only one claimant(parent only)" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        ),
      whereDoYouLive = Some("England")
      ))

      val auditData = ("1000", "2000", "3000", "4000", "5000", "true", "false","true", "false", "8000")
      val auditDataMap = mockAuditDataHelper.getResultSummaryAuditData(auditData,claimants,  children)

      auditDataMap shouldBe Map("numberOfChildren" -> "2","tfcAmount" -> "1000",  "tcAmount" ->  "2000",
        "escAmount" ->"3000","tcAmountByUser" ->  "4000", "ucAmountByUser" ->"5000", "tfcEligibility" -> "true",
        "tcEligibility" -> "false", "escEligibilityParent" -> "true", "escEligibilityPartner" ->  "",
        "user-single" -> "true", "user-couple" -> "false", "location" -> "England",  "annualChildCareCost" -> "8000",
        "Child1Cost" -> "200.0", "Child2Cost" -> "500.0", "parentPreviousIncome" -> "0", "parentCurrentIncome" -> "0",
        "partnerPreviousIncome" -> "", "partnerCurrentIncome" -> "")

    }

    "audit data map for no childcare cost" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = None,
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        )
      ))

      val auditData = ("1000", "2000", "3000", "4000", "5000", "true", "false","true", "false", "8000")
      val auditDataMap = mockAuditDataHelper.getResultSummaryAuditData(auditData,claimants,  children)

      auditDataMap shouldBe Map("numberOfChildren" -> "1", "tfcAmount" -> "1000",  "tcAmount" ->  "2000",
        "escAmount" ->"3000","tcAmountByUser" ->  "4000", "ucAmountByUser" ->"5000", "tfcEligibility" -> "true",
        "tcEligibility" -> "false", "escEligibilityParent" -> "true", "escEligibilityPartner" ->  "",
        "user-single" -> "true", "user-couple" -> "false","location" -> "",  "annualChildCareCost" -> "8000",
        "Child1Cost" -> "", "parentPreviousIncome" -> "0", "parentCurrentIncome" -> "0",
        "partnerPreviousIncome" -> "", "partnerCurrentIncome" -> "")

    }

    "audit data map for both parent and partner incomes" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = None,
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        ),
        previousIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(20000.00),
          otherIncome = Some(200.00)

        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(25000.00),
          otherIncome = Some(300.00)
        )
      )
      ),
        _root_.models.claimant.Claimant(
        id = 2,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        ),
        previousIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(30000.00),
          otherIncome = Some(100.00)

        )),
        currentIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(40000.00),
          otherIncome = Some(600.00)
        )
        )))

      val auditData = ("1000", "2000", "3000", "4000", "5000", "true", "false","true", "false", "8000")
      val auditDataMap = mockAuditDataHelper.getResultSummaryAuditData(auditData,claimants,  children)

      auditDataMap shouldBe Map("numberOfChildren" -> "1", "tfcAmount" -> "1000",  "tcAmount" ->  "2000",
        "escAmount" ->"3000","tcAmountByUser" ->  "4000", "ucAmountByUser" ->"5000", "tfcEligibility" -> "true",
        "tcEligibility" -> "false", "escEligibilityParent" -> "true", "escEligibilityPartner" ->  "false",
        "user-single" -> "false", "user-couple" -> "true","location" -> "",  "annualChildCareCost" -> "8000",
        "Child1Cost" -> "", "parentPreviousIncome" -> "20200.0", "parentCurrentIncome" -> "25300.0",
        "partnerPreviousIncome" -> "30100.0", "partnerCurrentIncome" -> "40600.0")

    }


    "audit data map for only parent and partner" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        )
      ),
        _root_.models.claimant.Claimant(
          id = 2,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        ))

      val auditData = ("1000", "2000", "3000", "4000", "5000", "true", "false","true", "false", "8000")
      val auditDataMap = mockAuditDataHelper.getResultSummaryAuditData(auditData,claimants,  children)

      auditDataMap shouldBe Map("numberOfChildren" -> "2", "tfcAmount" -> "1000",  "tcAmount" ->  "2000",
        "escAmount" ->"3000","tcAmountByUser" ->  "4000", "ucAmountByUser" ->"5000", "tfcEligibility" -> "true",
        "tcEligibility" -> "false", "escEligibilityParent" -> "true", "escEligibilityPartner" ->  "false",
        "user-single" -> "false", "user-couple" -> "true",  "location" -> "", "annualChildCareCost" -> "8000",
        "Child1Cost" -> "200.0", "Child2Cost" -> "500.0", "parentPreviousIncome" -> "0", "parentCurrentIncome" -> "0",
        "partnerPreviousIncome" -> "0", "partnerCurrentIncome" -> "0")

    }

    "audit benefits data map for single claimant and children" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = true,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        )
      ))

      val auditDataMap = mockAuditDataHelper.getClaimantChildrenBenefitsAuditData(claimants,  children)

      auditDataMap shouldBe  Map("parentDisabled" -> "true", "parentSeverelyDisabled" -> "false",
        "parentIncomeBenefits" -> "false", "parentCarersAllowance" -> "false",
        "parentNoBenefits" -> "false", "partnerDisabled" -> "", "partnerSeverelyDisabled" -> "",
        "partnerIncomeBenefits" -> "", "partnerCarersAllowance" -> "","partnerNoBenefits" -> "",
        "Child1Disabled" -> "false",  "Child1SeverelyDisabled" -> "false", "Child1Blind" -> "false",
        "Child1NoBenefit" -> "true", "Child2Disabled" -> "false",  "Child2SeverelyDisabled" -> "true",
        "Child2Blind" -> "false", "Child2NoBenefit" -> "false"
      )
    }

    "audit benefits data map for parent, partner and children" in {

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = true,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        )
      ),
        _root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        ))

      val auditDataMap = mockAuditDataHelper.getClaimantChildrenBenefitsAuditData(claimants,  children)

      auditDataMap shouldBe  Map("parentDisabled" -> "true", "parentSeverelyDisabled" -> "false",
        "parentIncomeBenefits" -> "false", "parentCarersAllowance" -> "false",
        "parentNoBenefits" -> "false", "partnerDisabled" -> "false", "partnerSeverelyDisabled" -> "false",
        "partnerIncomeBenefits" -> "false", "partnerCarersAllowance" -> "false","partnerNoBenefits" -> "true",
        "Child1Disabled" -> "false",  "Child1SeverelyDisabled" -> "false", "Child1Blind" -> "false",
        "Child1NoBenefit" -> "true", "Child2Disabled" -> "false",  "Child2SeverelyDisabled" -> "true",
        "Child2Blind" -> "false", "Child2NoBenefit" -> "false"
      )
    }

    "audit childcare costs per age for a child who is born today and a one year old" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val oneYearBeforeToday = LocalDate.now().minusYears(1).toString() + "T00:00:00Z"
      val childDOB = LocalDate.parse(oneYearBeforeToday, formatter)
      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(childDOB),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )))


      val auditDataMap = mockAuditDataHelper.getChildcareCostPerAgeAuditData(children)

      auditDataMap shouldBe  Map("Child0" -> "200.0", "Child1" -> "500.0")
    }

    "audit childcare costs per age for a child who is born today and a older child" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val childDOB = LocalDate.parse("1998-01-28T00:00:00Z", formatter)
      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(childDOB),
          childCareCost = None,
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )))


      val auditDataMap = mockAuditDataHelper.getChildcareCostPerAgeAuditData(children)

      auditDataMap shouldBe  Map("Child0" -> "200.0")
    }

    "audit childcare costs per age for 2 children of the same age and one of different age" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val fourYearBeforeToday = LocalDate.now().minusYears(4).toString() + "T00:00:00Z"
      val twoYearBeforeToday = LocalDate.now().minusYears(2).toString() + "T00:00:00Z"
      val childDOB = LocalDate.parse(fourYearBeforeToday, formatter)
      val childDOB2 = LocalDate.parse(twoYearBeforeToday, formatter)
      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(childDOB),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(childDOB),
          childCareCost = Some(BigDecimal(250.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )),
        _root_.models.child.Child(
          id = 0,
          name = "Child3",
          dob = Some(childDOB2),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          ))
      )


      val auditDataMap = mockAuditDataHelper.getChildcareCostPerAgeAuditData(children)

      auditDataMap shouldBe  Map("Child4" -> "200.0,250.0", "Child2" -> "500.0")
    }
  }
}
