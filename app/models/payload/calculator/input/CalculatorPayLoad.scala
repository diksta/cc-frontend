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

package models.payload.calculator.input

import models.payload.calculator.input.esc.ESCEligibility
import models.payload.calculator.input.tfc.TFCEligibility
import models.payload.calculator.input.tc.TCEligibility
import play.api.libs.json.{JsPath, Writes}
import play.api.libs.functional.syntax._

/**
 * Created by elsie on 11/03/16.
 */

case class Eligibility (
                        tc: Option[TCEligibility] = None,
                        tfc: Option[TFCEligibility] = None,
                        esc: Option[ESCEligibility] = None
                        )

object Eligibility {
  implicit val eligibilityWrites : Writes[Eligibility] = (
    (JsPath \ "tc").write[Option[TCEligibility]] and
      (JsPath \ "tfc").write[Option[TFCEligibility]] and
        (JsPath \ "esc").write[Option[ESCEligibility]]
    )(unlift(Eligibility.unapply))
}

case class CalculatorInput(
                     payload: CalculatorPayload
                       )

object CalculatorInput {
  implicit val calculatorPayloadWrites: Writes[CalculatorInput] = (JsPath \ "payload").write[CalculatorPayload].contramap { (calculator: CalculatorInput) => calculator.payload }
}

case class CalculatorPayload(
                     eligibility: Eligibility
                     )

object CalculatorPayload {
  implicit val calculatorInputWrites: Writes[CalculatorPayload] = (JsPath \ "eligibility").write[Eligibility].contramap { (calculator: CalculatorPayload) => calculator.eligibility }
}
