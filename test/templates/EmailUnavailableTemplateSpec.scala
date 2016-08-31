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

class EmailUnavailableTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "EmailUnavailable template" should {

    "Display title of page" in {
      val template = views.html.emailUnavailable(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "We're experiencing technical difficulties"
    }

    "Display retry register (return to email register page)" in {
      val template = views.html.emailUnavailable(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("emailRegisterLink").attr("href") shouldBe "/childcare-calculator/emailRegistration/keep-me-updated"
      doc.getElementById("emailRegisterLink").text() shouldBe "try again in a few minutes."
    }

    "Display back button (return to results page)" in {
      val template = views.html.emailUnavailable(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("resultsControllerLink").attr("href") shouldBe "/childcare-calculator/schemes/result"
    }

  }
}
