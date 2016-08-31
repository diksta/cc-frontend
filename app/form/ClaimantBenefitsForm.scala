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

import models.pages.ClaimantBenefitsPageModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

/**
 * Created by user on 19/02/16.
 */
trait ClaimantBenefitsKeys {
  val incomeBenefit = "incomeBenefit"
  val disabilityBenefit = "disabilityBenefit"
  val severeDisabilityBenefit = "severeDisabilityBenefit"
  val carerAllowanceBenefit = "carerAllowanceBenefit"
  val noBenefit = "noBenefit"
}

trait ClaimantBenefitsForm extends ClaimantBenefitsKeys {

  val isParent : Boolean

  val form : Form[ClaimantBenefitsPageModel] = Form(mapping(
    incomeBenefit -> boolean,
    disabilityBenefit -> boolean,
    severeDisabilityBenefit -> boolean,
    carerAllowanceBenefit -> boolean,
    noBenefit -> boolean
  )(ClaimantBenefitsPageModel.apply)(ClaimantBenefitsPageModel.unapply)
    .verifying(Messages("cc.claimant.benefit.no.benefits.selected"), x => x.selection)
    .verifying(if(isParent) Messages("cc.claimant.benefit.invalid.parent.benefits.selected") else Messages("cc.claimant.benefit.invalid.partner.benefits.selected"), x =>{ if (x.selection) x.validSelection else true})
  )
}

class ClaimantBenefitsFormInstance(parent : Boolean = true) extends ClaimantBenefitsForm {
  override val isParent = parent
}
