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
import form.ClaimantBenefitsFormInstance
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec


class ClaimantBenefitsTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "ClaimantBenefits template" should {

    "POST to /childcare-calculator/parent/benefits" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(1))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/parent/benefits"
    }

    /*"POST to /childcare-calculator/partner/benefits" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 2, routes.ChildDetailsController.onPageLoad(2))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/partner/benefits"
    }*/

    "display the title (Parent)" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(1))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Do you get any of these benefits?"
    }

    "display the title (Partner)" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 2, routes.ChildDetailsController.onPageLoad(1))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Do they get any of these benefits?"
    }

    "display the input field - incomeBenefit" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(1))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("incomeBenefit") should not be null
    }

    "display the hint text" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(1))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("selectAll-lede").text() shouldBe "Select all that apply"
    }

    "display the back button (Parent)" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(3))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/children/details/3"
    }

    "display the back button (Partner)" in {
      val form = (new ClaimantBenefitsFormInstance).form
      val template = views.html.claimantBenefits(form, 2, routes.DoYouLiveWithPartnerController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/parent/liveWithPartner"
    }

    "pre-populate the form" in {
      val form = (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "true"
        ))
      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(3))(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("incomeBenefit").hasAttr("checked") shouldBe false
          doc.getElementById("disabilityBenefit").hasAttr("checked") shouldBe false
          doc.getElementById("severeDisabilityBenefit").hasAttr("checked") shouldBe false
          doc.getElementById("carerAllowanceBenefit").hasAttr("checked") shouldBe false
          doc.getElementById("noBenefit").hasAttr("checked") shouldBe true
        }
      )
    }

    "display errors when no benefits are selected (Parent)" in {
      val form = (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "false"
        ))
      form.fold(
        errors => {
          val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(3))(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when none benefits and income benefit is selected (Parent)" in {
      val form = (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "true",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "true"
        ))
      form.fold(
        errors => {
          val template = views.html.claimantBenefits(form, 1, routes.ChildDetailsController.onPageLoad(3))(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when no benefits are selected (Partner)" in {
      val form = (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "false"
        ))
      form.fold(
        errors => {
          val template = views.html.claimantBenefits(form, 2, routes.ChildDetailsController.onPageLoad(3))(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
        },
        success => success should not be None
      )
    }

    "display errors when none benefits and income benefit is selected (Partner)" in {
      val form = (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "true",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "true"
        ))
      form.fold(
        errors => {
          val template = views.html.claimantBenefits(form, 2, routes.ChildDetailsController.onPageLoad(3))(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
        },
        success => success should not be None
      )
    }
  }
}
