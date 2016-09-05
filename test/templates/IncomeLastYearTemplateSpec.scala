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
import models.pages.income.{ClaimantIncomeLastYearBenefitsPageModel, ClaimantIncomeLastYearOtherPageModel, ClaimantIncomeLastYearEmploymentPageModel, ClaimantIncomeLastYearPageModel}
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import org.jsoup.Jsoup
import form.{ClaimantIncomeLastYearFormInstance, ClaimantIncomeLastYearForm}
import play.api.test.Helpers._
/**
 * Created by adamconder on 24/02/2016.
 */
class IncomeLastYearTemplateSpec extends UnitSpec with FakeCCApplication with CCSession {

  implicit val request = FakeRequest()
  val taxYears = ("2014", "2015")

  "IncomeLastYear parent template" when {

    "rendering the template" should {

      "POST to /parent/income/last" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/parent/income/last"
      }


      "display the title" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe s"6 April ${taxYears._1} to 5 April ${taxYears._2} Last year's income"
      }

      "display the previous tax year" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByClass("heading-secondary").first().text() shouldBe s"6 April ${taxYears._1} to 5 April ${taxYears._2}"
      }

      "display the employment radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("employment_selection") should not be null
      }

      "display the other income radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other_selection") should not be null
      }

      "display the benefits radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits_selection") should not be null
      }

      "pre-populate the form" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "20000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => errors.errors shouldBe empty,
          success => {
            val template = views.html.incomeLastYear(form, taxYears, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("employment-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("employment-income").attr("value") shouldBe "20000.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "200.00"

            doc.getElementById("other-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("other-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("benefits-income").attr("value") shouldBe "200.00"
          }
        )
      }

      "pre-populate the form validation message if pension is greater than income for parent" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Your pension payments cannot be more than your income from employment"
          },
          success => {
            val template = views.html.incomeLastYear(form, taxYears, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("employment-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("employment-income").attr("value") shouldBe "200.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "200.00"

            doc.getElementById("other-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("other-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("benefits-income").attr("value") shouldBe "200.00"
          }
        )
      }

      "display the continue button" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("next-button") should not be null
      }

      "display the back button when parent income last year" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/parent/benefits"
      }


    }

    "rendering the employment fields" should {

      "render the income and pension field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))

        doc.getElementById("employment-pension") should not be null
      }

    }

    "rendering the other income fields" should {

      "render the other income field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other-income") should not be null
      }

    }

    "rendering the benefits fields" should {

      "render the benefits income field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 1)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits-income") should not be null
      }

    }

    "rendering errors" should {

      "display errors when the form has errors" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "sfgdsxv",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => {
            val template = views.html.incomeLastYear(errors, taxYears, 1)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("employment-income-error-summary") should not be null
            doc.getElementById("employment-income-error-message") should not be null

          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(BigDecimal(200.00))
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(BigDecimal(200.00))
              )
            )
          }
        )
      }

    }

  }

  "IncomeLastYear partner template" when {

    "rendering the template" should {


      "POST to /partner/income/last" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByTag("form").first().attr("action") shouldBe "/childcare-calculator-qa/partner/income/last"
      }

      "display the title" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("page-title").text() shouldBe s"6 April ${taxYears._1} to 5 April ${taxYears._2} Your partner's income last year"
      }

      "display the previous tax year" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementsByClass("heading-secondary").first().text() shouldBe s"6 April ${taxYears._1} to 5 April ${taxYears._2}"
      }

      "display the employment radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("employment_selection") should not be null
      }

      "display the other income radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other_selection") should not be null
      }

      "display the benefits radio buttons" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits_selection") should not be null
      }

      "pre-populate the form" in {
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "15000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => errors.errors shouldBe empty,
          success => {
            val template = views.html.incomeLastYear(form, taxYears, 2)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("employment-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("employment-income").attr("value") shouldBe "15000.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "200.00"

            doc.getElementById("other-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("other-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("benefits-income").attr("value") shouldBe "200.00"
          }
        )
      }

      "pre-populate the form with validation message if pension greater than income for partner" in {
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Their pension payments cannot be more than their income from employment"
          },
          success => {
            val template = views.html.incomeLastYear(form, taxYears, 2)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("employment-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("employment-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("employment-income").attr("value") shouldBe "200.00"
            doc.getElementById("employment-pension").attr("value") shouldBe "200.00"

            doc.getElementById("other-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("other-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("other-income").attr("value") shouldBe "200.00"

            doc.getElementById("benefits-selection-true").hasAttr("checked") shouldBe true
            doc.getElementById("benefits-selection-false").hasAttr("checked") shouldBe false
            doc.getElementById("benefits-income").attr("value") shouldBe "200.00"
          }
        )
      }

      "display the continue button" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("next-button") should not be null
      }

      "display the back button when partner income last year" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("back-button").attr("href") shouldBe "/childcare-calculator-qa/partner/benefits"
      }

    }

    "rendering the employment fields" should {

      "render the income and pension field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))

        doc.getElementById("employment-pension") should not be null
      }

    }

    "rendering the other income fields" should {

      "render the other income field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("other-income") should not be null
      }

    }

    "rendering the benefits fields" should {

      "render the benefits income field" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form
        val template = views.html.incomeLastYear(form, taxYears, 2)(request)
        val doc = Jsoup.parse(contentAsString(template))
        doc.getElementById("benefits-income") should not be null
      }

    }

    "rendering errors" should {

      "display errors when the form has errors" in {
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(Map(
          "employment.selection" -> "true",
          "employment.income" -> "sfgdsxv",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        ))

        form.fold(
          errors => {
            val template = views.html.incomeLastYear(errors, taxYears, 2)(request)
            val doc = Jsoup.parse(contentAsString(template))
            doc.getElementById("error-summary-display") should not be null
            doc.getElementById("employment-income-error-summary") should not be null
            doc.getElementById("employment-income-error-message") should not be null

          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(BigDecimal(200.00))
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(BigDecimal(200.00))
              )
            )
          }
        )
      }

    }

  }

}
