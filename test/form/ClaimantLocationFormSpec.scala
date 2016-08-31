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

package form

import controllers.FakeCCApplication
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by elsie on 21/04/16.
 */
class ClaimantLocationFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar{

  "ClaimantLocationFormSpec" should {

    "accept when one location is selected" in {
      ClaimantLocationForm.form.bind(Map(
        "whereDoYouLive" -> "england"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some("england")
        )
    }

    "throw validation error when nothing is selected" in {
      ClaimantLocationForm.form.bind(Map(
        "whereDoYouLive" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must select an answer for where you live",
          success =>
            success should not be Some(true)
        )
    }

    "prepopulate the form with a value" in {
      val form = ClaimantLocationForm.form.fill(Some("england"))
      form.get shouldBe Some("england")
    }

  }

}
