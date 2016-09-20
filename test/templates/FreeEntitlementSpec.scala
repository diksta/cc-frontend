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
import models.pages.results
import models.pages.results.{EscVouchersAvailablePageModel, Scheme, ResultsPageModel, FreeEntitlementPageModel}
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class FreeEntitlementSpec extends UnitSpec with CCSession with FakeCCApplication {

  implicit val request = FakeRequest()



  "Free Entitlement template" should {
    "display 3-4 year old entitlement for England when child is 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
    }

    "display 3-4 year old entitlement for England when child is 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
    }

    "display 3-4 year old entitlement for England when children are 3 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
    }

    "display 3-4 year old additional 15 hours entitlement for England when children are 3 and 4 years old on 1st Sept 2017" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = true,
        region = "england",
        tfcEligibility = true
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOldSept2017") should not be null
    }

    "display 2 and 3-4 year old entitlement for England when child is 2 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
      doc.getElementById("englandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for England when children are 2 and 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
      doc.getElementById("englandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for England when children are 2 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
      doc.getElementById("englandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for England when children are 2, 3 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = true,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") should not be null
      doc.getElementById("englandTwoYearsOld") should not be null
    }

    "DO NOT display any free entitlement message for England when children are NOT 2, 3 or 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") shouldBe null
      doc.getElementById("englandTwoYearsOld") shouldBe null
    }

    "display 3-4 year old entitlement for Scotland when child is 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
    }

    "display 3-4 year old entitlement for Scotland when child is 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
    }

    "display 3-4 year old entitlement for Scotland when children are 3 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for Scotland when child is 2 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
      doc.getElementById("scotlandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for Scotland when children are 2 and 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
      doc.getElementById("scotlandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for Scotland when children are 2 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = false,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
      doc.getElementById("scotlandTwoYearsOld") should not be null
    }

    "display 2 and 3-4 year old entitlement for Scotland when children are 2, 3 and 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = true,
        fourYearOld = true,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("scotlandThreeFourYearsOld") should not be null
      doc.getElementById("scotlandTwoYearsOld") should not be null
    }

    "DO NOT display any free entitlement message for Scotland when children are NOT 2, 3 or 4 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "scotland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") shouldBe null
      doc.getElementById("englandTwoYearsOld") shouldBe null
    }

    "display 2 and 3 year old entitlement for Wales when child is 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "wales"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("walesTwoThreeYearsOld") should not be null
    }


    "display 2 and 3 year old entitlement for Wales when child is 2 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "wales"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("walesTwoThreeYearsOld") should not be null
    }

    "display 2 and 3 year old entitlement for Wales when children are 2 and 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = true,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "wales"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("walesTwoThreeYearsOld") should not be null
    }

    "DO NOT display any free entitlement message for Wales when children are NOT 2 or 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "england"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("englandThreeFourYearsOld") shouldBe null
      doc.getElementById("englandTwoYearsOld") shouldBe null
    }

    "display 3 year old entitlement for Northern Ireland when child is 3 years old" in {
      val template = views.html.freeEntitlement(Some(FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = true,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "northern-ireland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("northernIrelandThreeYearsOld") should not be null
    }

    "DO NOT display any free entitlement message for Northern Ireland when children are NOT 3 years old" in {
      val template = views.html.freeEntitlement(Some(results.FreeEntitlementPageModel(
        twoYearOld = false,
        threeYearOld = false,
        fourYearOld = false,
        threeFourYearOldSep2017 = false,
        region = "northern-ireland"
      )))(request)
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("northernIrelandThreeYearsOld") shouldBe null
    }


      "show free entitlement for England if user is eligible for it" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = true,
            threeYearOld = false,
            fourYearOld = false,
            threeFourYearOldSep2017 = false,
            region = "england")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement-england") should not be null
      }

      "show free entitlement for Scotland if user is eligible for it" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = true,
            threeYearOld = false,
            fourYearOld = false,
            threeFourYearOldSep2017 = false,
            region = "scotland")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement-scotland") should not be null
      }

      "show free entitlement for Northern Ireland if user is eligible for it" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = false,
            threeYearOld = true,
            fourYearOld = false,
            threeFourYearOldSep2017 = false,
            region = "northern-ireland")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement-northern-ireland") should not be null
      }

      "show free entitlement for Wales if user is eligible for it" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = false,
            threeYearOld = true,
            fourYearOld = false,
            threeFourYearOldSep2017 = false,
            region = "wales")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement-wales") should not be null
      }

      "do not show free entitlement for Northern Ireland if user is not eligible for it" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = false,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = true,
            threeYearOld = false,
            fourYearOld = false,
            threeFourYearOldSep2017 = false,
            region = "northern-ireland")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("free-entitlement") shouldBe null
      }


      "email registration link when free entitlement available when child is 3 or 4 as on 1 sept 2017" in {
        val template = views.html.results(ResultsPageModel(
          annualCost = 9600,
          List(Scheme("TFC",1920), Scheme("ESC",1866), Scheme("TC", 205)),
          tcAmountByUser = 0,
          ucAmountByUser = 0,
          tfcEligibility = true,
          tcEligibility = false,
          escEligibility = false,
          freeEntitlement = Some(FreeEntitlementPageModel(
            twoYearOld = true,
            threeYearOld = false,
            fourYearOld = false,
            threeFourYearOldSep2017 = true,
            region = "england")),
          escVouchersAvailable = EscVouchersAvailablePageModel()
        ))(request)
        val doc = Jsoup.parse(contentAsString(template))

        doc.getElementById("tfcHowToApplyLink").attr("href") shouldBe "/childcare-calculator/emailRegistration/freeEntitlement"
      }
  }
}
