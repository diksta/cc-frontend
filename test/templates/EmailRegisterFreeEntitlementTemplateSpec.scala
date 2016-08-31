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
import form.EmailRegistrationForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec


class EmailRegisterFreeEntitlementTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {

  implicit val request = FakeRequest()

  "EmailRegisterFreeEntitlement Template" should {

    "POST to emailRegister" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/emailRegistration/freeEntitlement"

    }

    "dispaly the title" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Keep me updated"

    }
    "display the cancel button" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator/schemes/result"
    }

    "display the input field" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("emailAddress") should not be null
    }

    "display the yes/no radio buttons" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("childrenDobSelection") should not be null
    }

    "display the keep me updated button" in {
      val form = EmailRegistrationForm.form
      val template = views.html.emailRegistrationFreeEntitlement(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("next-button") should not be null
    }

    "display errors when the form has errors - dob save not selected" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test@test.com",
        "childrenDobSelection" -> ""
      )).fold(
        errors => {
          val template = views.html.emailRegistrationFreeEntitlement(errors)(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("childrenDobSelection-error-message") should not be null
        },
        success => success should not be Some(true)
      )
    }

    "display errors when the form has errors - email address entered is invalid" in {
      EmailRegistrationForm.form.bind(Map(
        "emailAddress" -> "test@@test.com",
        "childrenDobSelection" -> "false"
      )).fold(
          errors => {
            val template = views.html.emailRegistrationFreeEntitlement(errors)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("emailAddress-error-message") should not be null
          },
          success => success should not be Some(true)
        )
    }
  }
}
