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

package models.payload.calculator.output

import models.payload.calculator.output.esc.ESCCalculatorOutput
import models.payload.calculator.output.tc.TCCalculatorOutput
import models.payload.calculator.output.tfc.TFCCalculatorOutput
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CalculatorOutput(
                             calculation: Calculation
                             )

object CalculatorOutput {
  implicit val calculatorOutputFormat: Reads[CalculatorOutput] =
    (JsPath \ "calculation").read[Calculation].map { calculation => CalculatorOutput(calculation)}
}

case class Calculation(
                        tc: Option[TCCalculatorOutput] = None,
                        tfc: Option[TFCCalculatorOutput] = None,
                        esc: Option[ESCCalculatorOutput] = None
                        )

object Calculation {
  implicit val AwardPeriodReads: Reads[Calculation] = (
    (JsPath \ "tc").read[Option[TCCalculatorOutput]] and
      (JsPath \ "tfc").read[Option[TFCCalculatorOutput]] and
        (JsPath \ "esc").read[Option[ESCCalculatorOutput]]
    )(Calculation.apply _)  }
