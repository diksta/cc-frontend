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
import models.pages.{ChildDetailsDisabilityPageModel, ChildDetailsPageModel}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 05/02/2016.
 */
class ChildDetailsFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "ChildDetailsForm" should {

    "accept a valid date of birth and a disability option" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val disability = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled =false,
        certifiedBlind = false,
        nonDisabled = true)
      val outputModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)
      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }

    "accept a valid date of birth and a certified blind disability option is selected" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val disability = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled =false,
        certifiedBlind = true,
        nonDisabled = false)
      val outputModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)
      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "true",
        "disability.benefitNone" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }

    "accept a valid date of birth and a severe disabled disability option is selected" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val disability = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled =true,
        certifiedBlind = false,
        nonDisabled = false)
      val outputModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)
      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "true",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }

    "accept a valid date of birth and a disabled disability option is selected" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val disability = ChildDetailsDisabilityPageModel(
        disabled = true,
        severelyDisabled =false,
        certifiedBlind = false,
        nonDisabled = false)
      val outputModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)
      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "true",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }

    "throw a ValidationError when empty date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "",
        "dateOfBirth.month" -> "",
        "dateOfBirth.year" -> "",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.date.of.birth.mandatory"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when birth year is empty  for date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "02",
        "dateOfBirth.year" -> "",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when special characters are entered for date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "£$",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect date for date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "41",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect month for date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "24",
        "dateOfBirth.month" -> "18",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect year for date of birth is provided" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "24",
        "dateOfBirth.month" -> "18",
        "dateOfBirth.year" -> "0000",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when no disability option is selected" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "false"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.no.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when none and certified blind disability option is selected" in {
      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "true",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.invalid.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when none and disabled disability option is selected" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "true",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.invalid.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when none and severe disabled disability option is selected" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "true",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.invalid.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError if year field is negative" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "-153",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.date.of.birth.mandatory"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError if year field length is not 4" in {

      ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "0053",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.details.date.of.birth.mandatory"),
          success =>
            success shouldBe true
        )
    }

    "pre-populate the form with a value" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val disability = ChildDetailsDisabilityPageModel(
        disabled = false,
        severelyDisabled =false,
        certifiedBlind = true,
        nonDisabled = false)
      val childDetailModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)
      val form = ChildDetailsForm.form.fill(childDetailModel)
      form.get shouldBe childDetailModel
    }
  }
}
