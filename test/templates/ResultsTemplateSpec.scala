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

import controllers.{ESCVouchersController, FakeCCApplication}
import controllers.keystore.CCSession
import models.pages.results.{EscVouchersAvailablePageModel, Scheme, FreeEntitlementPageModel, ResultsPageModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class ResultsTemplateSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()

  val resultsPageModel = ResultsPageModel(
    annualCost = 9600,
    List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
    ucAmountByUser = 0,
    tcAmountByUser = 0,
    tfcEligibility = false,
    tcEligibility = false,
    escEligibility = false,
    escVouchersAvailable = EscVouchersAvailablePageModel()
  )
  val resultsPageModelWithTCInput = ResultsPageModel(
    annualCost = 9600,
    Seq(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
    ucAmountByUser = 0,
    tcAmountByUser = 50,
    tfcEligibility = false,
    tcEligibility = false,
    escEligibility = false,
    escVouchersAvailable = EscVouchersAvailablePageModel()
  )
  val resultsPageModelWithUCInput = ResultsPageModel(
    annualCost = 9600,
    List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
    ucAmountByUser = 50,
    tcAmountByUser = 0,
    tfcEligibility = false,
    tcEligibility = false,
    escEligibility = false,
    escVouchersAvailable = EscVouchersAvailablePageModel()
  )


  "Results template" should {

    "load results page" in {
      val template = views.html.results(resultsPageModel)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementsByTag("main").hasClass("resultsPage") shouldBe true
    }

    "display the title" in {
      val template = views.html.results(resultsPageModel)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Compare support options for your family"
    }

    "test sub heading text for various scenarios" should {

      "display the annualCost" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        //hard coding the cost because in template it adds the comma automatically so test is failing as 9,600 doesn't match 9600
        doc.getElementById("annualCost").text() shouldBe "Your childcare costs are: £9,600 a year."
      }

    }

    "test eligibility cases" should {
      "display the TFC entitlement if eligible" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tfcEntitlement").text() shouldBe "£1,920 a year"
      }

      "display the ESC entitlement if eligible" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escEntitlement").text() shouldBe "£1,866 a year"
      }


      "show TFC eligibility by color if user is eligible" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tfcHeading").hasClass("schemeName") shouldBe true
      }

      "display the message that both parent and partner are eligible for esc when they select both their employer offer ESC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 480)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel(true, Some(true))
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escBothEligible") should not be null
        doc.getElementById("escOnlyOneEligible") shouldBe null
        doc.getElementById("notEligibleForESC") shouldBe null
      }

      "display the message that only parent is eligible for esc when they select only parent's employer offer ESC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 480)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel(true, Some(false))
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escOnlyOneEligible") should not be null
        doc.getElementById("escBothEligible") shouldBe null
        doc.getElementById("notEligibleForESC") shouldBe null
        doc.getElementById("escEligibleDefaultMessage") shouldBe null
      }

      "display the message that only partner is eligible for esc when they select only partner's employer offer ESC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 480)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel(false, Some(true))
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escOnlyOneEligible") should not be null
        doc.getElementById("escBothEligible") shouldBe null
        doc.getElementById("notEligibleForESC") shouldBe null
        doc.getElementById("escEligibleDefaultMessage") shouldBe null
      }

      "display the default ESC message when parent's employer offer ESC and there is no partner" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 480)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel(true, None)
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escOnlyOneEligible") shouldBe null
        doc.getElementById("escBothEligible") shouldBe null
        doc.getElementById("notEligibleForESC") shouldBe null
        doc.getElementById("escEligibleDefaultMessage") should not be null
      }

      "display the message that parent or partner are not eligible for esc when they select their employer doesn't offer ESC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",0), Scheme("TC",205), Scheme("TFC", 480)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel(false, Some(false))
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escOnlyOneEligible") shouldBe null
        doc.getElementById("escBothEligible") shouldBe null
        doc.getElementById("escEligibleDefaultMessage") shouldBe null
        doc.getElementById("notEligibleForESC") should not be null
      }

      "show not eligible for TFC by color if user is not eligible - heading column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tfcHeading").hasClass("not-eligible") shouldBe true
      }

      "show not eligible for TFC by color if user is not eligible - eligibility column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",1866), Scheme("TC",205), Scheme("TFC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tfcEligibility").hasClass("not-eligible") shouldBe true
      }

      "show not eligible for ESC by color if user is not eligible - heading column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1000), Scheme("TC",205), Scheme("ESC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escHeading").hasClass("not-eligible") shouldBe true
      }

      "show not eligible for ESC by color if user is not eligible - eligibility column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1000), Scheme("TC",205), Scheme("ESC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escEligibility").hasClass("not-eligible") shouldBe true
      }

      "show not eligible for TC by color if user is not eligible - heading column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",2000), Scheme("TFC",1000), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcHeading").hasClass("not-eligible") shouldBe true
      }

      "show not eligible for TC by color if user is not eligible - eligibility column" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("ESC",2000), Scheme("TFC",1000), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcEligibility").hasClass("not-eligible") shouldBe true
      }

      "display TC eligibility amount when user is eligible and not receiving TC/UC already" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcEntitlement").text() shouldBe "£205 a year"
      }

      "display TC eligibility amount that is entered by the user" in {
        val template = views.html.results(resultsPageModelWithTCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcEntitlement").text() shouldBe "£50 a year"
      }

      "display UC eligibility amount that is entered by the user" in {
        val template = views.html.results(resultsPageModelWithTCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcEntitlement").text() shouldBe "£50 a year"
      }

      "display you are on this scheme text if user enters they receive TC/UC" in {
        val template = views.html.results(resultsPageModelWithTCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("currentlyOnThisScheme").text() shouldBe "You are currently on this scheme"
      }

      "not show warning message for TFC if user is not eligible for TC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",0), Scheme("ESC",1866), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("additionalSupportTFCMessage") shouldBe null
      }

      "not show warning message for ESC if user is not eligible for TC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",0), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("additionalSupportESCMessage") shouldBe null
      }

      "not show additional support message for TC if user is not eligible for TC" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("additionalSupportTCMessage") shouldBe null
      }

      "display you may be eligible for tfc message if childcare cost is zero and parent eligible" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 0,
          List(Scheme("TFC",0), Scheme("ESC",1866), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = true,
          tcEligibility = false,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tfcEligibleZeroChildcare") should not be null
        doc.getElementById("additionalSupportTFCMessage") should not be null
      }

      "display Keep me updated page when link button pressed and when childcare cost is not zero but parent ineligible" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 6000,
          List(Scheme("TFC",0), Scheme("ESC",620), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = true,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("additionalSupportESCMessage") should not be null
        doc.getElementById("tfcHowToApplyLink").attr("href") shouldBe "/childcare-calculator/emailRegistration/keep-me-updated"

      }

      "display you may be eligible for esc message if childcare cost is zero and parent eligible" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 0,
          List(Scheme("TFC",0), Scheme("ESC",0), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = true,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escEligibleZeroChildcare") should not be null
        doc.getElementById("additionalSupportESCMessage") should not be null
      }

      "display you may have childcare cost for esc message if esc savings are zero and parent eligible" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 2000,
          List(Scheme("TFC",0), Scheme("ESC",0), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = true,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("escEligibleNoSavings") should not be null
      }

      "display you may be eligible for tc message if tc savings are zero" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 2000,
          List(Scheme("TFC",0), Scheme("ESC",0), Scheme("TC", 0)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = true,
          escEligibility = false,
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcEligibleNoSavings") should not be null
      }

    }

    "test TC warning messages" should {
      "show generic warning message for TFC if user is eligible for TC" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingTCGenericMessageForTFC") should not be null
      }

      "show warning message for TFC if user is getting tc" in {
        val template = views.html.results(resultsPageModelWithTCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingTCSpecificMessageForTFC") should not be null
      }

      "show warning message for TFC if user is getting uc" in {
        val template = views.html.results(resultsPageModelWithUCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingUCSpecificMessageForTFC") should not be null
      }

      "show generic warning message for ESC if user is eligible for TC" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingTCGenericMessageForESC") should not be null
      }

      "show generic warning message for ESC if user is getting tc" in {
        val template = views.html.results(resultsPageModelWithTCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingTCGenericMessageForESC") should not be null
      }

      "show warning message for ESC if user is getting uc" in {
        val template = views.html.results(resultsPageModelWithUCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingUCSpecificMessageForESC") should not be null
      }

      "show warning message for TC if eligible for TC or getting TC/UC" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("losingTFCGenericMessageForTC") should not be null
      }
      

      "not show TC how it works section if user is already receiving TC/UC" in {
        val template = views.html.results(resultsPageModelWithUCInput)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("tcHowItWorksLink") shouldBe null
      }
    }

    "test free entitlement" should {
      "do not show free entitlement if user is not eligible for it" in {
        val template = views.html.results(resultsPageModel)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement") shouldBe null
      }
    }

    "email registration link when no free entitlement " in {
      val template = views.html.results(resultsPageModel)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("tfcHowToApplyLink").attr("href") shouldBe "/childcare-calculator/emailRegistration/keep-me-updated"
    }

    "feedback survey link" in {
      val template = views.html.results(resultsPageModel)(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("feedbackSurveyLink").attr("href") shouldBe "https://goo.gl/YQeGFu"
    }

  }
}
