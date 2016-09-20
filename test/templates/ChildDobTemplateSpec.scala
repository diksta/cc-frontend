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

import controllers.keystore.CCSession
import controllers.{FakeCCApplication, routes}
import form.ChildDobForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 10/02/2016.
 */
class ChildDobTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "ChildDobTemplate template" should {

    "POST to /childcare-calculator/children/dob/1" in {
      val form = ChildDobForm.form
      val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/children/dob/1"
    }

    "display the title" in {
      val form = ChildDobForm.form
      val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Child 1's date of birth"
    }

    "display the input field" in {
      val form = ChildDobForm.form
      val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("dateOfBirth") should not be null
    }

    "display the hint text" in {
      val form = ChildDobForm.form
      val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("dateOfBirth-hint").text() shouldBe "For example, 06 03 2006"
    }

    "display the back button" in {
      val form = ChildDobForm.form
      val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/children/number"
    }

    "pre-populate the form" in {
      val form = ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015"
      ))
      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.childDob(form, 1, routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("dateOfBirth.day").attr("value") shouldBe "14"
          doc.getElementById("dateOfBirth.month").attr("value") shouldBe "04"
          doc.getElementById("dateOfBirth.year").attr("value") shouldBe "2015"
        }
      )
    }

    "display errors when the form has date errors" in {
      val form = ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "",
        "dateOfBirth.month" -> "",
        "dateOfBirth.year" -> ""
      ))
      form.fold(
        errors => {
          val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(day incorrect) errors" in {
      val form = ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "34",
        "dateOfBirth.month" -> "12",
        "dateOfBirth.year" -> "2005"
      ))
      form.fold(
        errors => {
          val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(month incorrect) errors" in {
      val form = ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "31",
        "dateOfBirth.month" -> "14",
        "dateOfBirth.year" -> "2005"
      ))
      form.fold(
        errors => {
          val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when the form has date(year incorrect) errors" in {
      val form = ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "31",
        "dateOfBirth.month" -> "14",
        "dateOfBirth.year" -> "0000"
      ))
      form.fold(
        errors => {
          val template = views.html.childDob(form, 1,routes.HowManyChildrenController.onPageLoad())(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("dateOfBirth-error-summary") should not be null
        },
        success => success should not be None
      )
    }
  }
}
