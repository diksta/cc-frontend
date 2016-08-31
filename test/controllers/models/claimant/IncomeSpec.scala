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

package controllers.models.claimant

import controllers.FakeCCApplication
import models.claimant.Income
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


/**
 * Created by ben on 24/02/16.
 */
class IncomeSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "Income" should {

   "if employmentIncome is defined isEmploymentIncomeSelected should return true" in {
     val model = Income(
     employmentIncome = Some(BigDecimal(1000.00)),
     pension = None
     )
     model.isEmploymentIncomeSelected shouldBe Some(true)
   }

    "if pension is defined isEmploymentIncomeSelected should return true" in {
      val model = Income(
        employmentIncome = None,
        pension = Some(BigDecimal(1000.00))
      )
      model.isEmploymentIncomeSelected shouldBe Some(true)
    }

    "if employmentIncome and pension is not defined isEmploymentIncomeSelected should return false" in {
      val model = Income(
        employmentIncome = None,
        pension = None
      )
      model.isEmploymentIncomeSelected shouldBe Some(false)
    }

    "if otherIncome is defined isOtherIncomeSelected should return true" in {
      val model = Income(
        otherIncome = Some(BigDecimal(1000.00))
      )
      model.isOtherIncomeSelected shouldBe Some(true)
    }

    "if otherIncome is not defined isOtherIncomeSelected should return false" in {
      val model = Income(
        otherIncome = None
      )
      model.isOtherIncomeSelected shouldBe Some(false)
    }

    "if benefits is defined isBenefitsSelected should return true" in {
      val model = Income(
        benefits = Some(BigDecimal(1000.00))
      )
      model.isBenefitsSelected shouldBe Some(true)
    }

    "if benefits is not defined isBenefitsSelected should return false" in {
      val model = Income(
        benefits = None
      )
      model.isBenefitsSelected shouldBe Some(false)
    }
  }

}
