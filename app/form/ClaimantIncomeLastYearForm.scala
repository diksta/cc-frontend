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

import config.ApplicationConfig
import models.pages.income.{ClaimantIncomeLastYearBenefitsPageModel, ClaimantIncomeLastYearEmploymentPageModel, ClaimantIncomeLastYearOtherPageModel, ClaimantIncomeLastYearPageModel}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages

/**
 * Created by adamconder on 22/02/2016.
 */

trait ClaimantIncomeLastYearFormKeys {
  val employment = "employment"
  val other = "other"
  val benefits = "benefits"

  val selection = "selection"
  val income = "income"
  val pension = "pension"
}
trait ParentPartner {
  protected val isPartner : Boolean
}

trait BenefitConstraints extends ParentPartner {

  def benefitAmountNotProvidedConstraint : Constraint[ClaimantIncomeLastYearBenefitsPageModel] = Constraint("cc.claimant.income.last.year.benefits.empty")({
    benefit =>
      benefit.selection match {
        case Some(selection) =>
          if (selection) {
            // selection is 'Yes'
            benefit.amount match {
              case Some(_) =>
                // an amount is provided
                Valid
              case None =>
                // no amount provided
                Invalid(if (isPartner) Messages("cc.partner.income.last.year.benefits.empty") else Messages("cc.parent.income.last.year.benefits.empty"))
            }
          } else {
            Valid
          }
        case None =>
          // no selection
          Invalid(Messages("cc.claimant.income.last.year.benefits.no.selection"))
      }
  })

}

trait OtherIncomeConstraints extends ParentPartner {

  def otherIncomeNotProvidedConstraint : Constraint[ClaimantIncomeLastYearOtherPageModel] = Constraint("cc.claimant.income.last.year.other.income.empty")({
    otherIncome =>
      otherIncome.selection match {
        case Some(selection) =>
          if(selection) {
            // selection is 'Yes'
            otherIncome.income match {
              case Some(_) =>
                // an amount is provided
                Valid
              case None =>
                // no amount provided
                Invalid(if (isPartner) Messages("cc.partner.income.last.year.other.income.empty") else Messages("cc.parent.income.last.year.other.income.empty"))
            }
          } else {
            // selection is 'No'
            Valid
          }
        case None =>
          // no selection
          Invalid(Messages("cc.claimant.income.last.year.other.income.no.selection"))
      }
  })

}

trait EmploymentIncomeConstraints extends ParentPartner {

  def employmentIncomeNotProvidedConstraint : Constraint[ClaimantIncomeLastYearEmploymentPageModel] = Constraint("cc.claimant.income.last.year.income.empty")({
    employmentIncome =>
      employmentIncome.selection match {
        case Some(selection) =>
          if (selection) {
            // selection is 'Yes'
            employmentIncome.income match {
              case Some(income) => {
                // an amount is provided
                employmentIncome.pension match {
                  case Some(pension) =>
                    if ((pension * ApplicationConfig.noOfMonths) > income) {
                      Invalid(if(isPartner) Messages("cc.partner.income.last.year.pension.higher") else Messages("cc.parent.income.last.year.pension.higher"))
                    } else Valid
                  case _ => Valid
                }
              }
              case None =>
                // no amount provided
                Invalid(if(isPartner) Messages("cc.partner.income.last.year.income.empty") else Messages("cc.parent.income.last.year.income.empty"))
            }
          } else {
            // selection is 'No'
            Valid
          }
        case None =>
          // no selection
          Invalid(Messages("cc.claimant.income.last.year.income.no.selection"))
      }
  })
}

trait ClaimantAmountConstraint {

  val incomeLessThan999999 = (x : Option[BigDecimal]) => {
    x match {
      case Some(v) => v >= ApplicationConfig.minimumEmploymentIncome && v < ApplicationConfig.maximumEmploymentIncome
      case None => true // overridden by the form global validation
    }
  }
  val pensionLessThan9999 = (x : Option[BigDecimal]) => {
    x match {
      case Some(v) => v >= ApplicationConfig.minimumEmploymentIncome && v < ApplicationConfig.maximumPensionContribution
      case None => true
    }
  }
  val benefitsLessThan99999 = (x : Option[BigDecimal]) => {
    x match {
      case Some(v) => v >= ApplicationConfig.minimumEmploymentIncome && v < ApplicationConfig.maximumBenfitsIncome
      case None => true
    }
  }
}

trait ClaimantIncomeLastYearForm extends ClaimantIncomeLastYearFormKeys with BenefitConstraints with OtherIncomeConstraints with EmploymentIncomeConstraints with ClaimantAmountConstraint {
  val employmentMapping = mapping(
    selection -> optional(boolean),
    income -> optional(bigDecimal).verifying(Messages("cc.claimant.income.last.year.income.incorrect"), incomeLessThan999999),
    pension -> optional(bigDecimal).verifying(Messages("cc.claimant.income.last.year.pension.incorrect"), pensionLessThan9999)
  )(ClaimantIncomeLastYearEmploymentPageModel.apply)(ClaimantIncomeLastYearEmploymentPageModel.unapply)
    .verifying(employmentIncomeNotProvidedConstraint)

  val otherIncomeMapping = mapping(
    selection -> optional(boolean),
    income -> optional(bigDecimal).verifying(Messages("cc.claimant.income.last.year.other.income.incorrect"), incomeLessThan999999)
  )(ClaimantIncomeLastYearOtherPageModel.apply)(ClaimantIncomeLastYearOtherPageModel.unapply)
    .verifying(otherIncomeNotProvidedConstraint)

  val benefitsMapping = mapping(
    selection -> optional(boolean),
    income -> optional(bigDecimal).verifying(Messages("cc.claimant.income.last.year.benefits.incorrect"), benefitsLessThan99999)
  )(ClaimantIncomeLastYearBenefitsPageModel.apply)(ClaimantIncomeLastYearBenefitsPageModel.unapply)
    .verifying(benefitAmountNotProvidedConstraint)

  val form : Form[ClaimantIncomeLastYearPageModel] = Form(
    mapping(
      employment -> employmentMapping,
      other -> otherIncomeMapping,
      benefits -> benefitsMapping
    )(ClaimantIncomeLastYearPageModel.apply)(ClaimantIncomeLastYearPageModel.unapply)
  )
}

class ClaimantIncomeLastYearFormInstance(partner : Boolean = false) extends ClaimantIncomeLastYearForm  {
  override val isPartner = partner
}
