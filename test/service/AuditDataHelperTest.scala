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
        "user-single" -> "true", "user-double" -> "false", "location" -> "England",  "annualChildCareCost" -> "8000",
        "Child1Cost" -> "200.0", "Child2Cost" -> "500.0")

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
        "user-single" -> "true", "user-double" -> "false","location" -> "",  "annualChildCareCost" -> "8000",
        "Child1Cost" -> "")

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
        "user-single" -> "false", "user-double" -> "true",  "location" -> "", "annualChildCareCost" -> "8000",
        "Child1Cost" -> "200.0", "Child2Cost" -> "500.0")

    }
  }
}
