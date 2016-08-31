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
import models.pages.income.{ClaimantIncomeLastYearBenefitsPageModel, ClaimantIncomeLastYearEmploymentPageModel, ClaimantIncomeLastYearOtherPageModel, ClaimantIncomeLastYearPageModel}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 22/02/2016.
 */
class ClaimantIncomeLastYearFormSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  "ClaimantIncomeLastYearForm" when {

    "selecting nothing" should {

      "throw a validation error for parent" in {
        (new ClaimantIncomeLastYearFormInstance).form.bind(
          Map(
            "employment.selection" -> "",
            "other.selection" -> "",
            "benefits.selection" -> ""
          )
        ).fold(
            errors => {
              errors.errors should not be empty
              val messages = errors.errors.map(x => x.message).toList
              messages should contain("You must answer the employment income question")
              messages should contain("You must answer the other income question")
              messages should contain("You must answer the benefits question")
            },
            success =>
              success should not be ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(false),
                  income = None,
                  pension = None
                ),
                other = ClaimantIncomeLastYearOtherPageModel(
                  selection = Some(false),
                  income = None
                ),
                benefits = ClaimantIncomeLastYearBenefitsPageModel(
                  selection = Some(false),
                  amount = None
                )
              )
          )
      }

      "throw a validation error for partner" in {
        (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(
          Map(
            "employment.selection" -> "",
            "other.selection" -> "",
            "benefits.selection" -> ""
          )
        ).fold(
            errors => {
              errors.errors should not be empty
              val messages = errors.errors.map(x => x.message).toList
              messages should contain("You must answer the employment income question")
              messages should contain("You must answer the other income question")
              messages should contain("You must answer the benefits question")
            },
            success =>
              success should not be ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(false),
                  income = None,
                  pension = None
                ),
                other = ClaimantIncomeLastYearOtherPageModel(
                  selection = Some(false),
                  income = None
                ),
                benefits = ClaimantIncomeLastYearBenefitsPageModel(
                  selection = Some(false),
                  amount = None
                )
              )
          )
      }

    }

    "selecting no x3" should {

      "(no x3) accept a valid minimum required payload parent" in {
        (new ClaimantIncomeLastYearFormInstance).form.bind(
          Map(
            "employment.selection" -> "false",
            "other.selection" -> "false",
            "benefits.selection" -> "false"
          )
        ).fold(
            errors =>
              errors.errors shouldBe empty,
            success =>
              success shouldBe ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(false),
                  income = None,
                  pension = None
                ),
                other = ClaimantIncomeLastYearOtherPageModel(
                  selection = Some(false),
                  income = None
                ),
                benefits = ClaimantIncomeLastYearBenefitsPageModel(
                  selection = Some(false),
                  amount = None
                )
              )
          )
      }

      "(no x3) accept a valid minimum required payload partner" in {
        (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(
          Map(
            "employment.selection" -> "false",
            "other.selection" -> "false",
            "benefits.selection" -> "false"
          )
        ).fold(
            errors =>
              errors.errors shouldBe empty,
            success =>
              success shouldBe ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(false),
                  income = None,
                  pension = None
                ),
                other = ClaimantIncomeLastYearOtherPageModel(
                  selection = Some(false),
                  income = None
                ),
                benefits = ClaimantIncomeLastYearBenefitsPageModel(
                  selection = Some(false),
                  amount = None
                )
              )
          )
      }

    }

    "selecting yes x3" should {

      "(yes, yes, yes) accept a valid minimum required payload parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
            errors =>
              errors.errors shouldBe empty,
            success =>
              success shouldBe ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(true),
                  income = Some(BigDecimal(200.00)),
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
          )
      }

      "(yes, yes, yes) accept a valid minimum required payload partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
            errors =>
              errors.errors shouldBe empty,
            success =>
              success shouldBe ClaimantIncomeLastYearPageModel(
                employment = ClaimantIncomeLastYearEmploymentPageModel(
                  selection = Some(true),
                  income = Some(BigDecimal(200.00)),
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
          )
      }

      "(yes, yes, yes) accept a valid payload with optional field provided parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "1.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(BigDecimal(200.00)),
                pension = Some(BigDecimal(1.00))
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
        )
      }

      "(yes, yes, yes) accept a valid payload with optional field provided partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "1.00",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(BigDecimal(200.00)),
                pension = Some(BigDecimal(1.00))
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
        )
      }

      "(yes, yes, yes) throw a validation error when no fields provided by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must tell us the total amount of benefits you got")
            messages should contain("You must tell us how much your other income was")
            messages should contain("You must tell us how much your income was")
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
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, yes) throw a validation error when no fields provided by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must give an answer for income from employment")
            messages should contain("You must give an answer for other income")
            messages should contain("You must tell us the total amount of benefits")
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
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(yes, yes, yes) throw a validation error when at least one required field is not provided by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must tell us how much your income was")
            messages should not contain "You must tell us the total amount of benefits you got"
            messages should not contain "You must tell us how much your other income was"
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
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(200.00)
              )
            )
          }
        )
      }

      "(yes, yes, yes) throw a validation error when at least one required field is not provided by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            val messages = errors.errors.map(x => x.message).toList

            messages should contain("You must give an answer for income from employment")
            messages should not contain "You must give an answer for other income"
            messages should not contain "You must tell us the total amount of benefits"
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
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(200.00)
              )
            )
          }
        )
      }

    }

    "selecting yes, no, no" should {

      "(yes, no, no) accept a valid payload by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) accept a valid payload by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) accept a valid payload when optional pension is provided by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "20000.00",
          "employment.pension" -> "100.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(20000.00),
                pension = Some(100.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }


        "(yes, no, no) accept a valid payload when optional pension is provided by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "30000.00",
          "employment.pension" -> "500.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(30000.00),
                pension = Some(500.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when income is not provided by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must tell us how much your income was"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }
      "(yes, no, no) throw a validation error when income is not provided by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must give an answer for income from employment"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when income providing characters by parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "sdasdasd",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when pension is greater than income for parent" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "2000",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Your pension payments cannot be more than your income from employment"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(2000),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when pension is greater than income for partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "2399",
          "employment.pension" -> "200",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "Their pension payments cannot be more than their income from employment"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(2399),
                pension = Some(200)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when income providing characters by partner" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "sdasdasd",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent income providing special characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "%%$@%200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner income providing special characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "%%$@%200.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent income providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "1000000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(1000000.00),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner income providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "1000000.00",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(1000000.00),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent income providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "-0.10",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(-0.10),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner income providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "-0.10",
          "employment.pension" -> "200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(-0.10),
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent pension providing characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "sdasdasd",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner pension providing characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "sdasdasd",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent pension providing special characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "%%$@%200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner pension providing special characters" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "%%$@%200.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = None,
                pension = Some(200.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent pension providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "10000.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = Some(10000.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when partner pension providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "10000.00",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = Some(10000.00)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(yes, no, no) throw a validation error when parent pension providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "-0.10",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = Some(-0.10)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }
      "(yes, no, no) throw a validation error when partner pension providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "200.00",
          "employment.pension" -> "-0.10",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(true),
                income = Some(200.00),
                pension = Some(-0.10)
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

    }

    "selecting no, yes, no" should {

      "(no, yes, no) accept a valid payload by parent" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) accept a valid payload by partner" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "200.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(200.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when other income is not provided by parent" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must tell us how much your other income was"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }
      "(no, yes, no) throw a validation error when other income is not provided by partner" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must give an answer for other income"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when parent providing characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "asdasdasdasd",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }
      "(no, yes, no) throw a validation error when partner providing characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "asdasdasdasd",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when parent providing special characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "  ^%%^@200.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when partner providing special characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "  ^%%^@200.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when parent providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "1000000.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(1000000.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when partner providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "1000000.00",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(1000000.00)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when parent providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "-0.10",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(-0.10)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

      "(no, yes, no) throw a validation error when partner providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "true",
          "other.income" -> "-0.10",
          "benefits.selection" -> "false",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 999999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(true),
                income = Some(-0.10)
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(false),
                amount = None
              )
            )
          }
        )
      }

    }

    "selecting no, no, yes" should {

      "(no, no, yes) accept a valid payload by parent" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(200.00)
              )
            )
          }
        )
      }

      "(no, no, yes) accept a valid payload by partner" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors shouldBe empty
          },
          success => {
            success shouldBe ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(200.00)
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when parent benefits income is not provided" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must tell us the total amount of benefits you got"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when partner benefits income is not provided" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> ""
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must tell us the total amount of benefits"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when parent providing characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "asdasdasd"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when partner providing characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "asdasdasd"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when parent providing special characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "   %%$%^200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when partner providing special characters" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "   %%$%^200.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "error.real"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = None
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when parent providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "100000.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(100000.00)
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when partner providing a value greater than the maximum" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "100000.00"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(100000.00)
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when parent providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "-0.10"
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(-0.10)
              )
            )
          }
        )
      }

      "(no, no, yes) throw a validation error when partner providing a value less than 0" in {
        val bind = Map(
          "employment.selection" -> "false",
          "employment.income" -> "",
          "employment.pension" -> "",
          "other.selection" -> "false",
          "other.income" -> "",
          "benefits.selection" -> "true",
          "benefits.income" -> "-0.10"
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.bind(bind)
        form.fold(
          errors => {
            errors.errors should not be empty
            errors.errors.head.message shouldBe "You must enter a number between 0 and 9999.99"
          },
          success => {
            success should not be ClaimantIncomeLastYearPageModel(
              employment = ClaimantIncomeLastYearEmploymentPageModel(
                selection = Some(false),
                income = None,
                pension = None
              ),
              other = ClaimantIncomeLastYearOtherPageModel(
                selection = Some(false),
                income = None
              ),
              benefits = ClaimantIncomeLastYearBenefitsPageModel(
                selection = Some(true),
                amount = Some(-0.10)
              )
            )
          }
        )
      }

    }

    "pre-populating the form" should {

      "pre-populate the parent form with a value" in {
        val input = ClaimantIncomeLastYearPageModel(
          employment = ClaimantIncomeLastYearEmploymentPageModel(
            selection = Some(false),
            income = None,
            pension = None
          ),
          other = ClaimantIncomeLastYearOtherPageModel(
            selection = Some(false),
            income = None
          ),
          benefits = ClaimantIncomeLastYearBenefitsPageModel(
            selection = Some(true),
            amount = Some(-0.10)
          )
        )
        val form = (new ClaimantIncomeLastYearFormInstance).form.fill(input)
        form.get shouldBe input
      }

      "pre-populate the partner form with a value" in {
        val input = ClaimantIncomeLastYearPageModel(
          employment = ClaimantIncomeLastYearEmploymentPageModel(
            selection = Some(false),
            income = None,
            pension = None
          ),
          other = ClaimantIncomeLastYearOtherPageModel(
            selection = Some(false),
            income = None
          ),
          benefits = ClaimantIncomeLastYearBenefitsPageModel(
            selection = Some(true),
            amount = Some(-0.10)
          )
        )
        val form = (new ClaimantIncomeLastYearFormInstance(partner = true)).form.fill(input)
        form.get shouldBe input
      }

    }

  }

}
