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
import form.DoYouLiveWithPartnerForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import controllers.{routes, FakeCCApplication}

/**
 * Created by elsie on 11/04/16.
 */
class DoYouHavePartnerTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {

  implicit val request = FakeRequest()
  val backUrl = routes.ClaimantLocationController.onPageLoad()

  "DoYouLiveWithPartner template" should {

    "POST to /parent/liveWithPartner" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/parent/liveWithPartner"
    }

    "display the title" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe s"Do you live with a partner?"
    }

    "display the yes/no radio buttons" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
     doc.getElementById("doYouLiveWithYourPartner") should not be null
    }

    "display the continue button" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("next-button") should not be null
    }


    "display the back button which goes back to location" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, routes.ClaimantLocationController.onPageLoad())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/parent/location"
    }

    "display the back button which goes back to hours" in {
      val form = DoYouLiveWithPartnerForm.form
      val template = views.html.doYouLiveWithPartner(form, routes.ClaimantHoursController.onPageLoadParent())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/parent/hours"
    }

    "display errors when the form has errors" in {
      DoYouLiveWithPartnerForm.form.bind(Map(
        "doYouLiveWithYourPartner" -> ""
      )).fold(
          errors => {
            val template = views.html.doYouLiveWithPartner(errors, backUrl)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("doYouLiveWithYourPartner-error-summary") should not be null
          },
          success => success should not be Some(true)
        )
    }

    "pre-populate the form" in {
      val form = DoYouLiveWithPartnerForm.form.bind(Map(
        "doYouLiveWithYourPartner" -> "true"
      ))

      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.doYouLiveWithPartner(form, backUrl)(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("doYouLiveWithYourPartner-true").hasAttr("checked") shouldBe true
        }
      )
    }

  }

}
