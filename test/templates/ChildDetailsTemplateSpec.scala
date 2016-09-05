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

package templates

import controllers.{routes, FakeCCApplication}
import controllers.keystore.CCSession
import form.ChildDetailsForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 10/02/2016.
 */
class ChildDetailsTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "ChildDetailsTemplate template" should {

    "POST to /childcare-calculator-qa/children/details/1" in {
      val form = ChildDetailsForm.form
      val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/children/details/1"
    }

    "display the title" in {
      val form = ChildDetailsForm.form
      val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Child 1's details"
    }

    "display the input field" in {
      val form = ChildDetailsForm.form
      val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("dateOfBirth") should not be null
    }

    "display the hint text" in {
      val form = ChildDetailsForm.form
      val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("dateOfBirth-hint").text() shouldBe "For example, 06 03 2006"
    }

    "display the back button" in {
      val form = ChildDetailsForm.form
      val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/children/number"
    }

    "pre-populate the form" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.childDetails(form, 1,"first", routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("dateOfBirth.day").attr("value") shouldBe "14"
          doc.getElementById("dateOfBirth.month").attr("value") shouldBe "04"
          doc.getElementById("dateOfBirth.year").attr("value") shouldBe "2015"
          doc.getElementById("disability_benefitDisabled").hasAttr("checked") shouldBe false
          doc.getElementById("disability_benefitSevereDisabled").hasAttr("checked") shouldBe false
          doc.getElementById("disability_benefitBlind").hasAttr("checked") shouldBe false
          doc.getElementById("disability_benefitNone").hasAttr("checked") shouldBe true
        }
      )
    }

    "display errors when the form has date errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "",
        "dateOfBirth.month" -> "",
        "dateOfBirth.year" -> "",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(day incorrect) errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "34",
        "dateOfBirth.month" -> "12",
        "dateOfBirth.year" -> "2005",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(month incorrect) errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "31",
        "dateOfBirth.month" -> "14",
        "dateOfBirth.year" -> "2005",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(year incorrect) errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "31",
        "dateOfBirth.month" -> "14",
        "dateOfBirth.year" -> "0000",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }


    "display errors when the form has date and disability errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "",
        "dateOfBirth.month" -> "",
        "dateOfBirth.year" -> "",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "false"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
          doc.getElementById("disability-error-summary") should not be null
        },
        success => success should not be None
      )
    }


    "display errors when the form has disability(no selection) errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "20",
        "dateOfBirth.month" -> "12",
        "dateOfBirth.year" -> "2005",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "false",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "false"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("disability-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has disability(benefit and none selected) errors" in {
      val form = ChildDetailsForm.form.bind(Map(
        "dateOfBirth.day" -> "20",
        "dateOfBirth.month" -> "12",
        "dateOfBirth.year" -> "2005",
        "disability.benefitDisabled" -> "false",
        "disability.benefitSevereDisabled" -> "true",
        "disability.benefitBlind" -> "false",
        "disability.benefitNone" -> "true"
      ))
      form.fold(
        errors => {
          val template = views.html.childDetails(form, 1,"first",routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("disability-error-summary") should not be null
        },
        success => success should not be None
      )
    }

  }
}
