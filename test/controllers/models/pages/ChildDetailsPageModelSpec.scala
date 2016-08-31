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
import models.pages.ChildDetailsDisabilityPageModel
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 05/02/2016.
 */
class ChildDetailsDisabilityPageModelSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "ChildDetailsDisabilityPageModel" should {

    "return true if they've made a correct selection" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = true
      )
      model.validSelection shouldBe true
    }

    "return true - valid selection - disabled benefit selected" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = false
      )
      model.validSelection shouldBe true
    }

    "return true - valid selection - severelyDisabled benefit selected" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = true,
        certifiedBlind = false,
        nonDisabled = false
      )
      model.validSelection shouldBe true
    }

    "return true - valid selection - certifiedBlind benefit selected" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = true,
        nonDisabled = false
      )
      model.validSelection shouldBe true
    }

    "return false if no selection" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = false
      )
      model.selection shouldBe false
    }

    "return true if something is selected" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = true
      )
      model.selection shouldBe true
    }


    "return false if all are selected" in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled = true,
        certifiedBlind = true,
        nonDisabled = true
      )
      model.validSelection shouldBe false
    }

    "return false if nonDisabled and disabled selected " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = true
      )
      model.validSelection shouldBe false
    }

    "return false if nonDisabled and severelyDisabled selected " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = true,
        certifiedBlind = false,
        nonDisabled = true
      )
      model.validSelection shouldBe false
    }

    "return false if nonDisabled and certifiedBlind selected " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = true,
        nonDisabled = true
      )
      model.validSelection shouldBe false
    }

    "return true if disabled selected - child is disabled " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled = false,
        certifiedBlind = true,
        nonDisabled = false
      )
      model.isChildDisabled shouldBe true
    }

    "return true if disabled and certifiedBlind selected - child is disabled " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled = false,
        certifiedBlind = true,
        nonDisabled = false
      )
      model.isChildDisabled shouldBe true
    }

    "return true if severelyDisabled selected - child is disabled " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = true,
        certifiedBlind = false,
        nonDisabled = false
      )
      model.isChildDisabled shouldBe true
    }

    "return false if nonDisabled selected - child is not disabled " in {
      val model = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled = false,
        certifiedBlind = false,
        nonDisabled = true
      )
      model.isChildDisabled shouldBe false
    }
  }
}
