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

package controllers.models.pages

import controllers.FakeCCApplication
import models.household.Benefits
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 19/02/16.
 */
class HouseholdBenefitsModelSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "HouseholdBenefitsPageModel" should {

    "return TC amount when there is some amount" in {
      val model = Benefits(
        tcAmount = Some(299.00),
        ucAmount = None
      )
      model.tcAmount shouldBe Some(299.00)
    }

    "return UC amount when there is some amount" in {
      val model = Benefits(
        tcAmount = None,
        ucAmount = Some(399.99)
      )
      model.ucAmount shouldBe Some(399.99)
    }

    "return None when there is no amount" in {
      val model = Benefits(
        tcAmount = None,
        ucAmount = None
      )
      model.tcAmount shouldBe None
      model.ucAmount shouldBe None
    }
  }
}
