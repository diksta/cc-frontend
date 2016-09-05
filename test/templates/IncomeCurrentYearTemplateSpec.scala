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
import form.ClaimantIncomeCurrentYearFormInstance
import models.pages.income._
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 24/02/2016.
 */
class IncomeCurrentYearTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {

  implicit val request = FakeRequest()
  val taxYearFrom = "2014"
  val taxYearTo = "2015"

  "IncomeCurrentYear template" when {

    "rendering the template" should {

      "POST to /claimant/income/current (Parent)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/parent/income/current"
      }

      "POST to /claimant/income/current (Partner)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 2, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/partner/income/current"
      }

      "display the title (Parent)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe s"6 April ${taxYearFrom} to 5 April ${taxYearTo} Is your income likely to change this year?"
      }

      "display the title (Partner)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 2, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe s"6 April ${taxYearFrom} to 5 April ${taxYearTo} Is your partner's income likely to change this year?"
      }

      "display the previous tax year" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByClass("heading-secondary").first().text() shouldBe s"6 April ${taxYearFrom} to 5 April ${taxYearTo}"
      }

      "display the employment radio buttons" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("employment_selection") should not be null
      }

      "display the other income radio buttons" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other_selection") should not be null
      }

      "display the benefits radio buttons" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits_selection") should not be null
      }

      "pre-populate the form (Parent)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "10.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        ))

        form.fold(
          errors => errors.errors shouldBe empty,
          success => {
            val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment_selection").hasAttr("checked") shouldBe true
            doc.getElementById("employment-income").attr("value") shouldBe "200.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "10.00"

            doc.getElementById("other_selection").hasAttr("checked") shouldBe true
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits_selection").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-amount").attr("value") shouldBe "200.00"
          }
        )
      }

      "pre-populate the form with error when pension is greater than income (Parent)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        ))

        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Your pension payments cannot be more than your income from employment"
          },
          success => {
            val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment_selection").hasAttr("checked") shouldBe true
            doc.getElementById("employment-income").attr("value") shouldBe "200.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "10.00"

            doc.getElementById("other_selection").hasAttr("checked") shouldBe true
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits_selection").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-amount").attr("value") shouldBe "200.00"
          }
        )
      }

      "pre-populate the form with error when pension is greater than income (Partner)" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        ))

        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Your pension payments cannot be more than your income from employment"
          },
          success => {
            val template = views.html.incomeCurrentYear(form, 2, taxYearFrom, taxYearTo)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment_selection").hasAttr("checked") shouldBe true
            doc.getElementById("employment-income").attr("value") shouldBe "200.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "5.00"

            doc.getElementById("other_selection").hasAttr("checked") shouldBe true
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits_selection").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-amount").attr("value") shouldBe "200.00"
          }
        )
      }

      "display the continue button" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("next-button") should not be null
      }

      "display the back button" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/parent/income/last"
      }
    }

    "rendering the employment fields" should {

      "render the income and pension field" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))

        doc.getElementById("employment-income") should not be null
        doc.getElementById("employment-pension") should not be null
      }

    }

    "rendering the other income fields" should {

      "render the other income field" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other-income") should not be null
      }

    }

    "rendering the benefits fields" should {

      "render the benefits income field" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form
        val template = views.html.incomeCurrentYear(form, 1, taxYearFrom, taxYearTo)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits-amount") should not be null
      }

    }

    "rendering errors" should {

      "display errors when the form has errors" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "sfgdsxv",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        ))

        form.fold(
          errors => {
            val template = views.html.incomeCurrentYear(errors, 1, taxYearFrom, taxYearTo)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("employment-income-error-summary") should not be null
            doc.getElementById("employment-income-error-message") should not be null
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200.00))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(200.00))
              )
            )
          }
        )
      }

      "display errors when the form has errors due to carers allowance" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance(currentYearSelection=true, isCarersAllowance=true)).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        ))

        form.fold(
          errors => {
            val template = views.html.incomeCurrentYear(errors, 1, taxYearFrom, taxYearTo, true)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-heading") should not be null
            doc.getElementById("benefits-error-summary") should not be null
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(200.00)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200.00))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(200.00))
              )
            )
          }
        )
      }


      "post successful when all fields are filled in and no errors displayed" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner=false, currentYearSelection=true, isCarersAllowance=true)).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "30000.0",
          "employment.pension" -> "1000.0",
          "other.selection" -> "true",
          "other.income" -> "2000.0",
          "benefits.selection" -> "true",
          "benefits.amount" -> "123.0"
        ))

        form.fold(
          errors => {
            val template = views.html.incomeCurrentYear(errors, 1, taxYearFrom, taxYearTo, true)(request)
            val doc = Jsoup.parse(contentAsString(template))
            errors.errors shouldBe empty
            errors.errors.head.message shouldBe empty
            doc.getElementById("error-summary-heading") shouldBe null
            doc.getElementById("selection-error-summary") shouldBe null
            doc.getElementById("-error-summary") shouldBe null
            doc.getElementById("employment-error-summary") shouldBe null
            doc.getElementById("other-error-summary") shouldBe null
            doc.getElementById("benefits-error-summary") shouldBe null
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(30000.00)),
                pension = Some(BigDecimal(1000.00))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(2000.00))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(123.00))
              )
            )
          }
        )
      }

      "post successful when all fields are filled in and no errors displayed partner" in {
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner=true, currentYearSelection=true, isCarersAllowance=true)).form.bind(Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "30000.0",
          "employment.pension" -> "1000.0",
          "other.selection" -> "true",
          "other.income" -> "2000.0",
          "benefits.selection" -> "true",
          "benefits.amount" -> "123.0"
        ))

        form.fold(
          errors => {
            val template = views.html.incomeCurrentYear(errors, 1, taxYearFrom, taxYearTo, true)(request)
            val doc = Jsoup.parse(contentAsString(template))
            errors.errors shouldBe empty
            errors.errors.head.message shouldBe empty
            doc.getElementById("error-summary-heading") shouldBe null
            doc.getElementById("selection-error-summary") shouldBe null
            doc.getElementById("-error-summary") shouldBe null
            doc.getElementById("employment-error-summary") shouldBe null
            doc.getElementById("other-error-summary") shouldBe null
            doc.getElementById("benefits-error-summary") shouldBe null
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(30000.00)),
                pension = Some(BigDecimal(1000.00))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(2000.00))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(123.00))
              )
            )
          }
        )
      }
    }
  }
}
