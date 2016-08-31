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

package models.payload.eligibility.output

import models.payload.eligibility.output.esc.ESCEligibilityOutput
import models.payload.eligibility.output.tc.TCEligibilityOutput
import models.payload.eligibility.output.tfc.TFCEligibilityOutput
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
 * Created by user on 18/03/16.
 */

case class EligibilityOutput(
                              eligibility: OutputEligibility
                              )

object EligibilityOutput {
  implicit val eligibilityOutputFormat: Reads[EligibilityOutput] =
    (JsPath \ "eligibility").read[OutputEligibility].map { eligibility => EligibilityOutput(eligibility)}

}

case class OutputEligibility (
                         tc: Option[TCEligibilityOutput] = None,
                         tfc: Option[TFCEligibilityOutput] = None,
                         esc: Option[ESCEligibilityOutput] = None
                         )

object OutputEligibility {
  implicit val eligibilityReads : Reads[OutputEligibility] = (
    (JsPath \ "tc").read[Option[TCEligibilityOutput]] and
      (JsPath \ "tfc").read[Option[TFCEligibilityOutput]] and
        (JsPath \ "esc").read[Option[ESCEligibilityOutput]]
    )(OutputEligibility.apply _)
}
