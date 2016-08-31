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

import controllers.FakeCCApplication
import controllers.keystore.CCSession
import form.ChildCareCostForm
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 15/02/16.
 */
class ChildCareCostTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "ChildCareCost template" should {

    "load childcare cost page" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, true, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/children/cost/1"
    }

    "display previous page when hit back button" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, true, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/children/details/1"
    }

    "display the title" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, true, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "How much will you spend on childcare for Child 1?"
    }

    "display the hint text" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, true, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByClass("form-hint").text shouldBe Messages("cc.childcare.cost.hint.text")
    }

    "display the input field" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, true, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("childCareCost") should not be null
    }

    "pre-populate the form" in {
      val form = ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "999.99",
        "childEducation" -> "false"
      ))

      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.childCareCost(form, true, 1)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("childCareCost").attr("value") shouldBe "999.99"
        }
      )
    }

    "display errors when the form has errors" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "-0.36",
        "childEducation" -> ""
      )).fold(
          errors => {
            val template = views.html.childCareCost(errors, true, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("childCareCost-error-summary") should not be null
          },
          success => {
            success.childCareCost should not be Some(0)
            success.childEducation should not be Some(false)
          }
        )
    }

  }
  "ChildEducation template" should {

    "load childcare education page" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/children/cost/1"
    }
    //show title
    "display the title" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Is Child 1 in further education?"
    }
    //show hint text
    "display the hint text" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByClass("form-hint").text shouldBe Messages("cc.childcare.education.hint.text")
    }
    //back button goes to benefits
    "display previous page when hit back button" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/children/details/1"
    }

    //yes option
    "select the radio button 'yes' option" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("childEducation-true") should not be null
    }
    //no option
    "select the radio button 'no' option" in {
      val form = ChildCareCostForm.form
      val template = views.html.childCareCost(form, false, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("childEducation-false") should not be null
    }

    //not selected
    "display errors when the form has errors" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "999.99",
        "childEducation" -> ""
      )).fold(
          errors => {
            val template = views.html.childCareCost(errors, false, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("childEducation-error-summary") should not be null
          },
          success => success should not be Some(0)
        )
    }

    "pre-populate the form" in {
      val form = ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "999.99",
        "childEducation" -> "true"
      ))

      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.childCareCost(form, false, 1)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("childEducation-true").attr("value") shouldBe "true"
        }
      )
    }

  }
}
