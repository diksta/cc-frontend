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
import form.ClaimantLocationForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec


class ClaimantLocationTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {

  implicit val request = FakeRequest()
  val backUrl = routes.ClaimantHoursController.onPageLoadParent()

  "WhereDoYouLive template" should {

    "POST to /parent/location" in {
      val form = ClaimantLocationForm.form
      val template = views.html.claimantLocation(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/parent/location"
    }

    "display the title" in {
      val form = ClaimantLocationForm.form
      val template = views.html.claimantLocation(form, routes.ESCVouchersController.onPageLoadParent())(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe s"Where do you live?"
    }

    "display the different location radio buttons" in {
      val form = ClaimantLocationForm.form
      val template = views.html.claimantLocation(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("whereDoYouLive") should not be null
    }

    "display the continue button" in {
      val form = ClaimantLocationForm.form
      val template = views.html.claimantLocation(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("next-button") should not be null
    }


    "display the back button" in {
      val form = ClaimantLocationForm.form
      val template = views.html.claimantLocation(form, backUrl)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/parent/hours"
    }

    "display errors when the form has errors" in {
      ClaimantLocationForm.form.bind(Map(
        "whereDoYouLive" -> ""
      )).fold(
          errors => {
            val template = views.html.claimantLocation(errors, backUrl)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("whereDoYouLive-error-summary") should not be null
          },
          success => success should not be Some(true)
        )
    }

    "pre-populate the form" in {
      val form = ClaimantLocationForm.form.bind(Map(
        "whereDoYouLive" -> "northern-ireland"
      ))

      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.claimantLocation(form, backUrl)(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("whereDoYouLive-northern-ireland").hasAttr("checked") shouldBe true
        }
      )
    }

  }

}
