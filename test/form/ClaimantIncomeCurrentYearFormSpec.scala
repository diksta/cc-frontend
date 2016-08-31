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

package form

import controllers.FakeCCApplication
import models.pages.income._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 22/02/2016.
 */
class ClaimantIncomeCurrentYearFormSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  "ClaimantIncomeCurrentYearForm" when {

    "selecting nothing" should {

      "throw a validation error" in {
        (new ClaimantIncomeCurrentYearFormInstance).form.bind(
          Map(
            "selection" -> "",
            "employment.selection" -> "",
            "other.selection" -> "",
            "benefits.selection" -> ""
          )
        ).fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList
            messages should contain("You must answer this question")
          },
          success =>
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(false),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "throw a validation error all" in {
        (new ClaimantIncomeCurrentYearFormInstance).form.bind(
          Map(
            "selection" -> "true"
          )
        ).fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList
            messages should contain("You must tell us which income is likely to change")
          },
          success =>
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(false),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }
    }

    "selecting no x3" should {

      "(no x3) accept a valid minimum required payload" in {
        (new ClaimantIncomeCurrentYearFormInstance).form.bind(
          Map(
            "selection" -> "false",
            "employment.selection" -> "false",
            "other.selection" -> "false",
            "benefits.selection" -> "false"
          )
        ).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(false),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

    }

    "selecting yes x3" should {

      "(yes, yes, yes, yes) accept a valid minimum required payload" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "300.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(300)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(200))
              )
            )
        )
      }

      "(yes, yes, yes, yes) accept a valid payload with optional field provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "500.00",
          "employment.pension" -> "10.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "450.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(500.00)),
                pension = Some(BigDecimal(10.00))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200.00))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(450.00))
              )
            )
        )
      }

      "(yes, yes, yes, yes) throw a validation error when no fields provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must give an answer for income from employment")
            messages should contain("You must give an answer for other income")
            messages should contain("You must tell us the total amount of benefits")
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
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, yes, yes) throw a validation error when at least one required field is not provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must give an answer for income from employment")
            messages should not contain "You must give an answer for other income"
            messages should not contain "You must tell us the total amount of benefits"
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
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(200.00)
              )
            )
          }
        )
      }

    }

    "selecting yes, yes, no, no" should {

      "(yes, yes, no, no) accept a valid payload" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(200.00),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) accept a valid payload when optional pension is provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "21000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(21000.00),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) accept a valid payload when optional pension is greater than income" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Your pension payments cannot be more than your income from employment"
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(200.00),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when income is not provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must give an answer for income from employment"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when income provided with characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "hjddgg",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when income providing special characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "%%$@%200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when income providing a value greater than the maximum" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "1000000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(1000000.00),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when income providing a value less than 0" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "-0.10",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(-0.10),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when pension providing characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "dkiijf",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
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
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when pension providing special characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "%%$@%200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when pension providing a value greater than the maximum" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "10000.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(200.00),
                pension = Some(10000.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when pension providing a value less than 0" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "-0.10",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(200.00),
                pension = Some(-0.10)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, no, no) throw a validation error when pension is greater than income for partner" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "2000.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = new ClaimantIncomeCurrentYearFormInstance(partner = true).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Their pension payments cannot be more than their income from employment"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(200.00),
                pension = Some(2000.00)
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

    }

    "selecting yes, no, yes, no" should {

      "(yes, no, yes, no) accept a valid payload" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, yes, no) throw a validation error when other income is not provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must give an answer for other income"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, yes, no) throw a validation error when providing characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "asdasdasdasd",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, yes, no) throw a validation error when providing special characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "  ^%%^@200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, yes, no) throw a validation error when providing a value greater than the maximum" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "1000000.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(1000000.00)
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, yes, no) throw a validation error when providing a value less than 0" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "-0.10",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(-0.10)
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
          }
        )
      }

    }

    "selecting yes, no, no, yes" should {

      "(yes, no, no, yes) accept a valid payload" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> "200.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(200.00)
              )
            )
          }
        )
      }

      "(yes, no, no, yes) throw a validation error when benefits income is not provided" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must tell us the total amount of benefits"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no, yes) throw a validation error when providing characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> "asdasdasd"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no, yes) throw a validation error when providing special characters" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> "   %%$%^200.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no, yes) throw a validation error when providing a value greater than the maximum" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> "100000.00"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(100000.00)
              )
            )
          }
        )
      }

      "(yes, no, no, yes) throw a validation error when providing a value less than 0" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.amount" -> "-0.10"
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = false,
                income = None,
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = false,
                income = None
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(-0.10)
              )
            )
          }
        )
      }

      "(yes, yes, yes, yes) accept a valid minimum required payload Parent" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "300.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance(isCarersAllowance = true, currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.parent.benefits.carers.selected")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(300)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(200))
              )
            )
        )
      }

      "(yes, yes, yes, yes) accept a valid minimum required payload Partner" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "300.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val form = new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = true, currentYearSelection = true).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.partner.benefits.carers.selected")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(300)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = true,
                amount = Some(BigDecimal(200))
              )
            )
        )
      }
    }

    "carer's allowance selected, did not enter benefits in last year income and answered income likely to change has No" in {
      val bind = Map(
        "selection" -> "false",
        "employment.selection" -> "false",
        "employment.income" -> "",
        "other.selection" -> "false",
        "other.income" -> "",
        "benefits.selection" -> "false",
        "benefits.amount" -> ""
      )

      val  lastYearIncome = Some(_root_.models.claimant.Income(
        employmentIncome = Some(BigDecimal(10000.00)),
        pension = Some(BigDecimal(3305.00)),
        otherIncome = Some(BigDecimal(6.00)),
        benefits = None
      ))
      val form = (new ClaimantIncomeCurrentYearFormInstance(isCarersAllowance = true, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
      form.fold(
        errors => {
          errors.errors should not be empty
          errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.parent.benefits.carers.selected")
        },
        success =>
          success shouldBe ClaimantIncomeCurrentYearPageModel(
            selection = Some(false),
            employment = ClaimantIncomeCurrentYearEmploymentPageModel(
              selection = false,
              income = None,
              pension = None
            ),
            other = ClaimantIncomeCurrentYearOtherPageModel(
              selection = false,
              income = None
            ),
            benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
              selection = false,
              amount = None
            )
          )
      )
    }


    "carer's allowance selected, entered benefits amount as 0 " in {
      val bind = Map(
        "selection" -> "true",
        "employment.selection" -> "true",
        "employment.income" -> "300.00",
        "other.selection" -> "true",
        "other.income" -> "200.00",
        "benefits.selection" -> "true",
        "benefits.amount" -> "0"
      )

      val form = (new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = true, currentYearSelection = true)).form.bind(bind)
      form.fold(
        errors => {
          errors.errors should not be empty
          errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.benefits.incorrect.carers.allowance.selected")
        },
        success =>
          success shouldBe ClaimantIncomeCurrentYearPageModel(
            selection = Some(true),
            employment = ClaimantIncomeCurrentYearEmploymentPageModel(
              selection = true,
              income = Some(BigDecimal(300)),
              pension = None
            ),
            other = ClaimantIncomeCurrentYearOtherPageModel(
              selection = true,
              income = Some(BigDecimal(200))
            ),
            benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
              selection = true,
              amount = Some(BigDecimal(0))
            )
          )
      )
    }

    "carer's allowance selected, entered valid benefits amount" in {
      val bind = Map(
        "selection" -> "true",
        "employment.selection" -> "true",
        "employment.income" -> "300.00",
        "other.selection" -> "true",
        "other.income" -> "200.00",
        "benefits.selection" -> "true",
        "benefits.amount" -> "1000"
      )

      val form = (new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = true, currentYearSelection = true)).form.bind(bind)
      form.fold(
        errors => {
          errors.errors should not be empty
          errors.errors.head.message shouldBe empty
        },
        success =>
          success shouldBe ClaimantIncomeCurrentYearPageModel(
            selection = Some(true),
            employment = ClaimantIncomeCurrentYearEmploymentPageModel(
              selection = true,
              income = Some(BigDecimal(300)),
              pension = None
            ),
            other = ClaimantIncomeCurrentYearOtherPageModel(
              selection = true,
              income = Some(BigDecimal(200))
            ),
            benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
              selection = true,
              amount = Some(BigDecimal(1000))
            )
          )
      )
    }

    "pre-populating the form" should {

      "pre-populate the form with a value" in {
        val input = ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment = ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = false,
            income = None,
            pension = None
          ),
          other = ClaimantIncomeCurrentYearOtherPageModel(
            selection = false,
            income = None
          ),
          benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(-0.10)
          )
        )
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(input)
        form.get shouldBe input
      }


      "throw error for parent when current year income amount is less than the pension amount provided in income previous year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "11000.00",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(12000.00)),
          pension = Some(BigDecimal(1000.00)),
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.parent.pension.higher")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = Some(BigDecimal(1000))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "throw error for partner when current year income amount is less than the pension amount provided in income previous year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "11000.00",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(12000.00)),
          pension = Some(BigDecimal(1000.00)),
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.partner.pension.higher")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = Some(BigDecimal(1000))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "post successful when parent last year income and pension provided and updated this year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "12000.00",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(12000.00)),
          pension = None,
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner = false, isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.partner.pension.higher")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "post successful when partner last year income and pension provided and updated this year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "12000.00",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(12000.00)),
          pension = None,
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe Messages("cc.claimant.income.current.year.partner.pension.higher")
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = None
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "post successful when parent last year income provided and pension updated this year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "12000.00",
          "employment.pension" -> "1000",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = None,
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner = false, isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
            errors.errors.head.message shouldBe empty
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = Some(BigDecimal(1000))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

      "post successful when partner last year income provided and pension updated this year" in {
        val bind = Map(
          "selection" -> "true",
          "employment.selection" -> "true",
          "employment.income" -> "12000.00",
          "employment.pension" -> "1000",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.amount" -> ""
        )
        val  lastYearIncome = Some(_root_.models.claimant.Income(
          employmentIncome = Some(BigDecimal(10000.00)),
          pension = None,
          otherIncome = Some(BigDecimal(6.00)),
          benefits = None
        ))
        val form = (new ClaimantIncomeCurrentYearFormInstance(partner = true, isCarersAllowance = false, lastYrIncome = lastYearIncome , currentYearSelection = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
            errors.errors.head.message shouldBe empty
          },
          success =>
            success shouldBe ClaimantIncomeCurrentYearPageModel(
              selection = Some(true),
              employment = ClaimantIncomeCurrentYearEmploymentPageModel(
                selection = true,
                income = Some(BigDecimal(12000)),
                pension = Some(BigDecimal(1000))
              ),
              other = ClaimantIncomeCurrentYearOtherPageModel(
                selection = true,
                income = Some(BigDecimal(200))
              ),
              benefits = ClaimantIncomeCurrentYearBenefitsPageModel(
                selection = false,
                amount = None
              )
            )
        )
      }

    }

  }

}
