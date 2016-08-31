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
import org.scalatest.mock.MockitoSugar

import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import org.jsoup.Jsoup
import form.HouseholdBenefitsForm
import play.api.test.Helpers._


class HouseholdBenefitsTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {
  implicit val request = FakeRequest()
  val backUrl = routes.DoYouLiveWithPartnerController.onPageLoad()

  "House hold income template" when {

    "rendering the template" should {

      "POST to results template" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/household/benefits"
      }

      "display DoYouLiveWithPartner page when hit back button" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/parent/liveWithPartner"
      }

      "display PartnerHours page when hit back button" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, routes.ClaimantHoursController.onPageLoadPartner())(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/partner/hours"
      }

      "display the title" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe "Do you get tax credits or Universal Credit?"
      }

      "display the tc amount field" in {
        val houseHoldBenefitsPageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection = true,
            ucBenefitSelection = false,
            noBenefitSelection = false,
            tcBenefitAmount = Some(400),
            ucBenefitAmount = None
          )
        )
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form.fill(houseHoldBenefitsPageModel), backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits-tcBenefitAmount").attr("value") shouldBe "400"
      }

      "display TC selection CheckBox" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits_tcBenefitSelection") should not be null
      }

      "display UC selection CheckBox" in {
        val template = views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits_ucBenefitSelection") should not be null
      }

      "pre-populate the form" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "false",
          "benefits.ucBenefitSelection" -> "true",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> "123"
        ))

        form.fold(
          errors => errors.errors shouldBe empty,
          success => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("benefits-ucBenefitAmount").attr("value") shouldBe "123"
          }
        )
      }

      "display errors when the selection has errors" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "true",
          "benefits.ucBenefitSelection" -> "true",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> ""
        ))
        form.fold(
          errors => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") should not be null
            doc.getElementById("benefits-error-summary") should not be null
          },
          success => {
            success shouldBe true
          }
        )
      }

      "display errors when the form has errors with empty amount" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "true",
          "benefits.ucBenefitSelection" -> "false",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> ""
        ))
        form.fold(
          errors => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") should not be null
            doc.getElementById("benefits-tcBenefitAmount-error-summary") should not be null
          },
          success => {
            success shouldBe true
          }
        )
      }

      "display errors when the form has errors with incorrect negative amount" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "true",
          "benefits.ucBenefitSelection" -> "false",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "-5",
          "benefits.ucBenefitAmount" -> ""
        ))
        form.fold(
          errors => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") should not be null
            doc.getElementById("benefits-tcBenefitAmount-error-summary") should not be null
          },
          success => {
            success shouldBe true
          }
        )
      }

      "display errors when the form has errors with incorrect amount" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "false",
          "benefits.ucBenefitSelection" -> "true",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> "0"
        ))
        form.fold(
          errors => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") should not be null
            doc.getElementById("benefits-ucBenefitAmount-error-summary") should not be null
          },
          success => {
            success shouldBe true
          }
        )
      }

      "post successfully when correct values entered" in {
        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "false",
          "benefits.ucBenefitSelection" -> "true",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> "500"
        ))
        form.fold(
          errors => {
            val template = views.html.householdBenefits(form, backUrl)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") shouldBe null
            doc.getElementById("tcBenefits-tcAmount-error-summary") shouldBe null
          },
          success => {
            success.benefits.ucBenefitSelection shouldBe true
            success.benefits.ucBenefitAmount shouldBe Some(500)
          }
        )
      }
    }
  }


}
