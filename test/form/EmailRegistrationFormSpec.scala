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
import models.pages.EmailRegisterPageModel
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class EmailRegistrationFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar{

  "EmailRegistrationForm" should {

      "accept a valid email and selection" in {
        EmailRegistrationForm.form.bind(Map(
          "emailAddress" -> "test.test2016@test.com",
          "childrenDobSelection" -> "true"
        )).fold(
            errors =>
              errors.errors shouldBe empty,
            success =>
              success shouldBe EmailRegisterPageModel(
                emailAddress = "test.test2016@test.com",
                childrenDobSelection = Some(true)
              )
          )
      }

    "accept a valid email and selection - emailAdddress lenght is 100" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeest@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe EmailRegisterPageModel(
              emailAddress = "teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeest@test.com",
              childrenDobSelection = Some(false)
            )
        )
    }

    "throw a validation error if childrenDobSelection not selected" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test@test.com",
        "childrenDobSelection" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.dob.not.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a validation error if email address is invalid - contains one special character" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test&t@abc.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.address.invalid"),
          success =>
            success shouldBe true
        )
    }


    "throw a validation error if email address is invalid - contains only numbers" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "4444445555@@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.address.invalid"),
          success =>
            success shouldBe true
        )
    }

    "throw a validation error if email address is invalid - contains special characters" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "tes&&%%$$tr@@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.address.invalid"),
          success =>
            success shouldBe true
        )
    }

    "throw a validation error if email address is invalid - contains 2 @" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test@@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.address.invalid"),
          success =>
            success shouldBe true
        )
    }

    "throw a validation error if email address is invalid - contains 2 dots" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test@test..com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.address.invalid"),
          success =>
            success shouldBe true
        )
    }

      "throw a validation error if email address is empty" in {
        EmailRegistrationForm.form.bind(Map(
          "emailAddress" -> "",
          "childrenDobSelection" -> "false"
        )).fold(
            errors =>
              errors.errors.head.message shouldBe Messages("cc.email.register.email.address.empty"),
            success =>
              success shouldBe true
          )
      }

    "throw a validation error if email address is length greater than 100" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeyyyyyyyyyyyeeeeest@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.email.register.email.max.length"),
          success =>
            success shouldBe true
        )
    }


    "pre-populate the form with a value" in {

      val form = EmailRegistrationForm.form.fill(EmailRegisterPageModel(emailAddress = "test.test016@test.com",childrenDobSelection = Some(true)))
      form.get shouldBe EmailRegisterPageModel(
        emailAddress = "test.test016@test.com",
        childrenDobSelection = Some(true)
      )
    }

  }
}
