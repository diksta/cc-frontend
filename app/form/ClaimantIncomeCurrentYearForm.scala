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
import models.pages.income.{ClaimantIncomeCurrentYearBenefitsPageModel, ClaimantIncomeCurrentYearEmploymentPageModel, ClaimantIncomeCurrentYearOtherPageModel, ClaimantIncomeCurrentYearPageModel}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages

/**
 * Created by user on 03/03/16.
 */

trait ClaimantIncomeCurrentYearFormKeys {
  val selection = "selection"

  val employment = "employment"
  val other = "other"
  val benefits = "benefits"

  val income = "income"
  val pension = "pension"
  val amount = "amount"
}

trait isPartner {
  protected val isPartner : Boolean
}

trait AdditionalInformation {
  protected val carersAllowance : Boolean
  //holds the ans to the question Is your income likely to change this year? - If nothing is selected it will be false, if yes or no is selected it would be true
  protected val isCurrentYearIncomeQuestAnswered : Boolean
  protected val lastYearIncome : Option[_root_.models.claimant.Income]

  def getLastYearBenefit = {

    lastYearIncome match {
      case Some(income) =>
        income.benefits match {
          case Some(x) => x
          case _ => 0
        }
      case _ => 0
    }
  }

  def getLastYearPension: BigDecimal = {
    lastYearIncome match {
      case Some(income) =>
        income.pension match {
          case Some(x) => x
          case _ => 0
        }
      case _ => 0
    }
  }

}

trait EmploymentIncomeCurrentYearConstraints extends isPartner with AdditionalInformation{

  def employmentIncomeNotProvidedConstraint : Constraint[ClaimantIncomeCurrentYearEmploymentPageModel] = Constraint("cc.claimant.income.current.year.employment.empty")({
    employmentIncome =>
      employmentIncome.selection match {
        case true =>
          //employment check box is selected
          employmentIncome.income match {
            case Some(income) =>
              // an amount is provided
              employmentIncome.pension match {
                case Some(pension) =>
                  if ((pension * ApplicationConfig.noOfMonths) > income) {
                    Invalid(if(isPartner) Messages("cc.partner.income.last.year.pension.higher") else Messages("cc.parent.income.last.year.pension.higher"))
                  } else Valid
                case _ =>
                  if ((getLastYearPension * ApplicationConfig.noOfMonths) > income) {
                    Invalid(if(isPartner) Messages("cc.partner.income.last.year.pension.higher") else Messages("cc.parent.income.last.year.pension.higher"))
                  } else Valid
              }
            case None =>
              // no amount provided
              Invalid(Messages("cc.claimant.income.current.year.employment.empty"))
          }
        case _ =>
          //employment check box is not selected
          Valid
      }
  })

}

trait OtherIncomeCurrentYearConstraints {
  def otherIncomeNotProvidedConstraint : Constraint[ClaimantIncomeCurrentYearOtherPageModel] = Constraint("cc.claimant.income.current.year.other.empty")({
    otherIncome =>
      otherIncome.selection match {
        case true =>
          otherIncome.income match {
            case Some(_) =>
              // an amount is provided
              Valid
            case None =>
              // no amount provided
              Invalid(Messages("cc.claimant.income.current.year.other.empty"))
          }
        case _ =>
          Valid
      }
  })
}

trait BenefitsIncomeCurrentYearConstraints extends isPartner  with AdditionalInformation {

  def benefitAmountNotProvidedConstraint: Constraint[ClaimantIncomeCurrentYearBenefitsPageModel] = Constraint("cc.claimant.income.current.year.benefits.empty")({
    benefit =>
      benefit.selection match {
        case true =>
          benefit.amount match {
            case Some(x) =>
              // an amount is provided
              Valid
            case _ =>
              // no amount provided
              Invalid(Messages("cc.claimant.income.current.year.benefits.empty"))
          }
        case _ =>
          //Do the below validation if you selected the ans as yes or no for the question is Is your income likely to change this year?
          if(carersAllowance && getLastYearBenefit == 0 && isCurrentYearIncomeQuestAnswered)
            Invalid(if(isPartner)Messages("cc.claimant.income.current.year.partner.benefits.carers.selected") else Messages("cc.claimant.income.current.year.parent.benefits.carers.selected"))
          else
            Valid
      }
  })
}

trait BenefitAmountConstraint extends AdditionalInformation {

  val benefitsAmountLessThan99999 = (x : Option[BigDecimal]) => {
    x match {
      case Some(v) => if(carersAllowance) v >= ApplicationConfig.minimumBenefitAmountWhenCarersAllowanceSelected && v < ApplicationConfig.maximumBenfitsIncome else v >= ApplicationConfig.minimumEmploymentIncome && v < ApplicationConfig.maximumBenfitsIncome
      case None => true
    }
  }
}

trait ClaimantIncomeCurrentYearForm extends ClaimantIncomeCurrentYearFormKeys with EmploymentIncomeCurrentYearConstraints with OtherIncomeCurrentYearConstraints
with BenefitsIncomeCurrentYearConstraints with ClaimantAmountConstraint with BenefitAmountConstraint {

  val employmentMapping = mapping(
    selection -> boolean,
    income -> optional(bigDecimal).verifying(Messages("cc.claimant.income.current.year.income.incorrect"), incomeLessThan999999),
    pension -> optional(bigDecimal).verifying(Messages("cc.claimant.income.current.year.pension.incorrect"), pensionLessThan9999)
  )(ClaimantIncomeCurrentYearEmploymentPageModel.apply)(ClaimantIncomeCurrentYearEmploymentPageModel.unapply)
    .verifying(employmentIncomeNotProvidedConstraint)

  val otherIncomeMapping = mapping(
    selection -> boolean,
    income -> optional(bigDecimal).verifying(Messages("cc.claimant.income.current.year.other.income.incorrect"), incomeLessThan999999)
  )(ClaimantIncomeCurrentYearOtherPageModel.apply)(ClaimantIncomeCurrentYearOtherPageModel.unapply)
    .verifying(otherIncomeNotProvidedConstraint)

  val benefitsMapping = mapping(
    selection -> boolean,
    amount -> optional(bigDecimal).verifying(if(carersAllowance) Messages("cc.claimant.income.current.year.benefits.incorrect.carers.allowance.selected") else  Messages("cc.claimant.income.current.year.benefits.incorrect"), benefitsAmountLessThan99999)
  )(ClaimantIncomeCurrentYearBenefitsPageModel.apply)(ClaimantIncomeCurrentYearBenefitsPageModel.unapply)
    .verifying(benefitAmountNotProvidedConstraint)

  val form : Form[ClaimantIncomeCurrentYearPageModel] = Form(
    mapping(
      selection -> optional(boolean).verifying(Messages("cc.claimant.income.current.year.no.selection"), x => x.isDefined && x.nonEmpty),
      employment -> employmentMapping,
      other -> otherIncomeMapping,
      benefits -> benefitsMapping
    )(ClaimantIncomeCurrentYearPageModel.apply)(ClaimantIncomeCurrentYearPageModel.unapply)
      .verifying(Messages("cc.claimant.income.current.year.income.empty"), x => x.validSelection )

  )
}

class ClaimantIncomeCurrentYearFormInstance(partner : Boolean = false, isCarersAllowance: Boolean = false, lastYrIncome : Option[_root_.models.claimant.Income] = None, currentYearSelection : Boolean = false) extends ClaimantIncomeCurrentYearForm {
  override val carersAllowance = isCarersAllowance
  override val isPartner = partner
  override val lastYearIncome =  lastYrIncome
  override val isCurrentYearIncomeQuestAnswered = currentYearSelection
}
