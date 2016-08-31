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

package models.payload.eligibility.input

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

/**
 * Created by user on 09/03/16.
 */
case class Claimant(
                     liveOrWork:  Boolean = true,
                     totalIncome: BigDecimal = BigDecimal(0.00),
                     earnedIncome: BigDecimal = BigDecimal(0.00),
                     hoursPerWeek: Double = 0.00,
                     isPartner: Boolean = false,
                     disability: Disability,
                     schemesClaiming: SchemesClaiming,
                     previousTotalIncome: BigDecimal = BigDecimal(0.00),
                     employerProvidesESC : Boolean = false,
                     elements: ClaimantsElements,
                     otherSupport: OtherSupport
                     )

object Claimant  {
  implicit val claimant : Writes[Claimant] = (
    (JsPath \ "liveOrWork").write[Boolean] and
      (JsPath \ "totalIncome").write[BigDecimal] and
      (JsPath \ "earnedIncome").write[BigDecimal] and
      (JsPath \ "hoursPerWeek").write[Double] and
      (JsPath \ "isPartner").write[Boolean] and
      (JsPath \ "disability").write[Disability] and
      (JsPath \ "schemesClaiming").write[SchemesClaiming] and
      (JsPath \ "previousTotalIncome").write[BigDecimal] and
      (JsPath \ "employerProvidesESC").write[Boolean] and
      (JsPath \ "elements").write[ClaimantsElements] and
      (JsPath \ "otherSupport").write[OtherSupport]
    )(unlift(Claimant.unapply))
}

case class ClaimantsElements(
                              vouchers : Boolean = true
                              )

object ClaimantsElements {
  implicit val claimantsElements : Writes[ClaimantsElements] =
    (JsPath \ "vouchers").write[Boolean].contramap { (claimantElements: ClaimantsElements) => claimantElements.vouchers }
}


case class OtherSupport(
                         disabilityBenefitsOrAllowances: Boolean = false,
                         severeDisabilityBenefitsOrAllowances: Boolean = false,
                         incomeBenefitsOrAllowances: Boolean = false,
                         carersAllowance: Boolean = false
                         )

object OtherSupport  {
  implicit val otherSupport : Writes[OtherSupport] = (
    (JsPath \ "disabilityBenefitsOrAllowances").write[Boolean] and
      (JsPath \ "severeDisabilityBenefitsOrAllowances").write[Boolean] and
      (JsPath \ "incomeBenefitsOrAllowances").write[Boolean] and
      (JsPath \ "carersAllowance").write[Boolean]
    )(unlift(OtherSupport.unapply))
}

case class SchemesClaiming(
                            esc: Boolean = false,
                            tfc: Boolean = false,
                            tc: Boolean = false,
                            uc: Boolean = false,
                            cg: Boolean = false
                            )

object SchemesClaiming{
  implicit val schemesClaiming: Writes[SchemesClaiming] = (
    (JsPath \ "esc").write[Boolean] and
      (JsPath \ "tfc").write[Boolean] and
      (JsPath \ "tc").write[Boolean] and
      (JsPath \ "uc").write[Boolean] and
      (JsPath \ "cg").write[Boolean]
    )(unlift(SchemesClaiming.unapply))
}
