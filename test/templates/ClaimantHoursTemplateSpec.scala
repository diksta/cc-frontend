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
import form.ClaimantHoursFormInstance
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class ClaimantHoursTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "ClaimantHoursTemplate" should {

      "POST to /parent/hours" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/parent/hours"
      }

    "POST to /partner/hours" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      val form = new ClaimantHoursFormInstance(parent = false, previousIncome, currentIncome).form
      val template = views.html.claimantHours(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/partner/hours"
    }

      "display the title (Parent)" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe "On average, how many hours a week do you usually work?"
      }

    "display the title (partner)" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      val form = new ClaimantHoursFormInstance(parent = false, previousIncome, currentIncome).form
      val template = views.html.claimantHours(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "On average, how many hours a week does your partner usually work?"
    }

      "display the input field" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("hours") should not be null
      }

      "display the hint text (Parent)" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByClass("form-hint").text shouldBe Messages("cc.claimant.hours.parent.hint.text")
      }

    "display the hint text (Partner)" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      val form = new ClaimantHoursFormInstance(parent = false, previousIncome, currentIncome).form
      val template = views.html.claimantHours(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByClass("form-hint").text shouldBe Messages("cc.claimant.hours.partner.hint.text")
    }

      "display current income on click of back button (Parent)" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/parent/income/current"
      }

      "display the continue button" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form
        val template = views.html.claimantHours(form, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("next-button") should not be null
      }

      "prepopulate the form" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
          "hours" -> "37.5"
        ))

        form.fold(
          errors => errors.errors shouldBe empty,
          success => {
            val template = views.html.claimantHours(form, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("hours").attr("value") shouldBe "37.5"
          }
        )
      }

      "display errors when the form has errors" in {
        val previousIncome = Some(models.claimant.Income(Some(2000.00)))
        val currentIncome = Some(models.claimant.Income(Some(2000.00)))
        val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
          "hours" -> "-11"
        )).fold(
            errors => {
              val template = views.html.claimantHours(errors, 1)(request)
              val doc = Jsoup.parse(contentAsString(template))
              doc.getElementById("error-summary-display") should not be null
              doc.getElementById("hours-error-summary") should not be null
            },
            success => success should not be Some(-11)
          )
      }
  }
}
