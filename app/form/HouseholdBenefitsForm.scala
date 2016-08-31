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
import models.pages.{BenefitsPageModel, HouseholdBenefitsPageModel}
import play.api.data.{Mapping, Form}
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.i18n.Messages
import uk.gov.voa.play.form._


/**
 * Created by user on 06/05/16.
 */

trait BenefitsFormKeys {
  val benefits = "benefits"
  val tcBenefitSelection = "tcBenefitSelection"
  val ucBenefitSelection = "ucBenefitSelection"
  val noBenefitSelection = "noBenefitSelection"
  val tcBenefitAmount = "tcBenefitAmount"
  val ucBenefitAmount = "ucBenefitAmount"
}


trait HouseholdBenefitsAmountConstraints {

   val householdAmountLessThan9999: Constraint[Option[BigDecimal]] = Constraint("cc.household.benefits.amount.range") {
    model =>
      if ((model.get.toInt > ApplicationConfig.minBenefitAmount && model.get.toInt <= ApplicationConfig.maxBenefitAmount))
        Valid
      else
        Invalid(Messages("cc.household.benefits.amount.range"))
  }

}


trait HouseholdBenefitsForm extends BenefitsFormKeys with HouseholdBenefitsAmountConstraints {

  def mandatoryIfAllEqual[T](pairs: Seq[(String, String)], mapping: Mapping[T],
                             showNestedErrors: Boolean = true, constraints:  Seq[Constraint[Option[T]]]): Mapping[Option[T]] = {
    val condition: Condition = x => (

      for (pair <- pairs) yield {
      if(x.get(pair._1).isEmpty && pair._2.eq("false"))
          true
        else
          x.get(pair._1).contains(pair._2)
      }
      ).forall(b => b)
    ConditionalMapping(condition, MandatoryOptionalMapping(mapping, constraints), None)
  }

  val benefitMapping = mapping(
    tcBenefitSelection -> boolean,
    ucBenefitSelection -> boolean,
    noBenefitSelection -> boolean,
    tcBenefitAmount ->  mandatoryIfAllEqual(Seq("benefits.tcBenefitSelection"-> "true", "benefits.ucBenefitSelection"-> "false", "benefits.noBenefitSelection"-> "false"), bigDecimal,constraints = Seq(householdAmountLessThan9999)),
    ucBenefitAmount ->  mandatoryIfAllEqual(Seq("benefits.ucBenefitSelection"-> "true", "benefits.tcBenefitSelection"-> "false", "benefits.noBenefitSelection"-> "false"), bigDecimal,constraints = Seq(householdAmountLessThan9999))
  )(BenefitsPageModel.apply)(BenefitsPageModel.unapply)
    .verifying(Messages("cc.household.benefits.no.selection"), x => x.selection)
    .verifying(Messages("cc.household.benefits.invalid.selection"), x =>{ if (x.selection) x.validSelection else true})

   val form : Form[HouseholdBenefitsPageModel] = Form(
    mapping(
      benefits -> benefitMapping
    )(HouseholdBenefitsPageModel.apply)(HouseholdBenefitsPageModel.unapply)

   )
}

object HouseholdBenefitsForm extends HouseholdBenefitsForm
