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

package models.claimant

import play.api.libs.json.Json

/**
 * Created by adamconder on 18/02/2016.
 */
case class Income(
                 employmentIncome : Option[BigDecimal] = None,
                 pension : Option[BigDecimal] = None,
                 otherIncome : Option[BigDecimal] = None,
                 benefits : Option[BigDecimal] = None
                   ) {

  def isEmploymentIncomeSelected() : Option[Boolean] = if (employmentIncome.isDefined || pension.isDefined) Some(true) else Some(false)

  def isOtherIncomeSelected()  : Option[Boolean] = if(otherIncome.isDefined) Some(true) else Some(false)

  def isBenefitsSelected()  : Option[Boolean] = if (benefits.isDefined) Some(true) else Some(false)
}

object Income {
  implicit val formats = Json.format[Income]
}
