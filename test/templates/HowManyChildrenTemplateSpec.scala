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
import form.HowManyChildrenForm
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 10/02/2016.
 */
class HowManyChildrenTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  "HowManyChildren template" should {

    "POST to /children/number" in {
      val form = HowManyChildrenForm.form
      val template = views.html.howManyChildren(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator/children/number"
    }

    "display the title" in {
      val form = HowManyChildrenForm.form
      val template = views.html.howManyChildren(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "How many children do you have?"
    }

    "display the input field" in {
      val form = HowManyChildrenForm.form
      val template = views.html.howManyChildren(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("numberOfChildren") should not be null
    }

    "display the hint text" in {
      val form = HowManyChildrenForm.form
      val template = views.html.howManyChildren(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("numberOfChildren-lede").text() shouldBe "Include children under the age of 20 that you're responsible for."
    }

    "display the back button" in {
      val form = HowManyChildrenForm.form
      val template = views.html.howManyChildren(form)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("back-button").attr("href") shouldBe "https://www.gov.uk/childcare-calculator"
    }

    "prepopulate the form" in {
      val form = HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "1"
      ))

      form.fold(
        errors => errors.errors shouldBe empty,
        success => {
          val template = views.html.howManyChildren(form)(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("numberOfChildren").attr("value") shouldBe "1"
        }
      )
    }

    "display errors when the form has errors" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "0"
      )).fold(
        errors => {
          val template = views.html.howManyChildren(errors)(request)
          val doc = Jsoup.parse(contentAsString(template))
          doc.getElementById("error-summary-display") should not be null
          doc.getElementById("numberOfChildren-error-summary") should not be null
        },
        success => success.get should not be Some(0)
      )
    }

  }

}
