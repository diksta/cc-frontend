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
import models.claimant.Income
import models.pages.ClaimantHoursPageModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.data.validation.{Constraint, Invalid, Valid}





trait ClaimantHoursKeys {
  val hours = "hours"
}

trait ClaimantHoursForm extends ClaimantHoursKeys  {

  val isParent : Boolean
  val lastYearIncome : Option[Income]
  val currentYearIncome : Option[Income]

  private def getEmploymentIncome(income : Option[Income]) = {
    income match {
      case Some(x) => x.employmentIncome
      case _ => None
    }
  }

  val form : Form[ClaimantHoursPageModel] = Form(
    mapping(
      hours -> optional(bigDecimal).verifying(if(isParent)Messages("cc.claimant.hours.parent.empty") else Messages("cc.claimant.hours.partner.empty"), x => x.isDefined && x.nonEmpty)
        .verifying(Messages("cc.claimant.hours.employment.income.entered.must.have.hours"), x => x match {
          case Some(x) =>
            val previousEmpIncome = getEmploymentIncome(lastYearIncome)
            val currentEmpIncome = getEmploymentIncome(currentYearIncome)
            if((x == 0 && currentEmpIncome.isDefined && currentEmpIncome.get != 0)
              || (x == 0 && currentEmpIncome.isEmpty && previousEmpIncome.isDefined && previousEmpIncome.get != 0)){
            false
          }
            else {
            true
          }
          case _ => true
        })
        .verifying(Messages("cc.claimant.hours.incorrect"), x => x match {
          case Some(x) => x >= ApplicationConfig.minimumHours && x <= ApplicationConfig.maximumHours
          case _ => true
        })
        .verifying(Messages("cc.claimant.hours.error.one.decimal.place"), x => x match {
          case Some(x) => if (x >= ApplicationConfig.minimumHours && x <= ApplicationConfig.maximumHours) {
            val regex = "(\\d{1,2}(\\.\\d{1})?)".r
            if (x.toString.matches(regex.toString()))
              true
            else
              false
          }
          else
            true
          case _ => true
        })
    )(ClaimantHoursPageModel.apply)(ClaimantHoursPageModel.unapply)
  )
}

class ClaimantHoursFormInstance(parent : Boolean = true, lastYearFormIncome : Option[Income], currentYearFormIncome : Option[Income] ) extends ClaimantHoursForm {
  override val isParent = parent
  override val lastYearIncome = lastYearFormIncome
  override val currentYearIncome = currentYearFormIncome
}
