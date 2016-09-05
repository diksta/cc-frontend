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
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import form.ESCVouchersFormInstance

class ESCVouchersTemplateSpec extends UnitSpec with FakeCCApplication with CCSession{

  implicit val request = FakeRequest()

  "ESCVouchers Template" should {

    "POST to /Parent/ESCVoucher" in {
      val form = new ESCVouchersFormInstance(parent = true).form
      val template = views.html.ESCVouchers(form, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/parent/escVouchers"
    }

    "POST to /Partner/ESCVoucher" in {
      val form = new ESCVouchersFormInstance(parent = false).form
      val template = views.html.ESCVouchers(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/partner/escVouchers"
    }

    "display the title (Parent)" in {
      val form = new ESCVouchersFormInstance(parent = true).form
      val template = views.html.ESCVouchers(form, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe s"Does your employer offer childcare vouchers?"
    }

    "display the title (Partner)" in {
      val form = new ESCVouchersFormInstance(parent = false).form
      val template = views.html.ESCVouchers(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe s"Does your partner's employer offer childcare vouchers?"
    }

    "display all radio button options" in {
      val form = new ESCVouchersFormInstance().form
      val template = views.html.ESCVouchers(form, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("doYouGetVouchers") should not be null
    }

    "display the continue button" in {
      val form = new ESCVouchersFormInstance().form
      val template = views.html.ESCVouchers(form, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("next-button") should not be null
    }

    "display the back button (parent)" in {
      val form = new ESCVouchersFormInstance(parent = true).form
      val template = views.html.ESCVouchers(form, 1)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/parent/hours"
    }

    "display the back button (partner)" in {
      val form = new ESCVouchersFormInstance(parent = false).form
      val template = views.html.ESCVouchers(form, 2)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/partner/hours"
    }

  }

}
