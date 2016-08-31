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

package models.pages

import play.api.libs.json.Json

/**
 * Created by user on 19/02/16.
 */
case class ClaimantBenefitsPageModel (
    incomeBenefit : Boolean,
    disabilityBenefit : Boolean,
    severeDisabilityBenefit : Boolean,
    carerAllowanceBenefit : Boolean,
    noBenefit: Boolean
){
  def validSelection : Boolean = !(noBenefit && (incomeBenefit || disabilityBenefit ||severeDisabilityBenefit || carerAllowanceBenefit))

  def selection : Boolean = !(!incomeBenefit && !disabilityBenefit && !severeDisabilityBenefit && !carerAllowanceBenefit && !noBenefit)

}
object ClaimantBenefitsPageModel {
  implicit val formats = Json.format[ClaimantBenefitsPageModel]
}
