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

package models.payload.calculator.input.tfc

import models.payload.calculator.input.Child
import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{Writes, JsPath}
import utils.CCFormat

/**
 * Created by user on 11/03/16.
 */

case class TFCEligibility(
                           from: LocalDate,
                           until: LocalDate,
                           householdEligibility: Boolean,
                           periods: List[TFCPeriod]
                           )


object TFCEligibility extends CCFormat{
  implicit val tfcEligibilityFormat: Writes[TFCEligibility] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
        (JsPath \ "householdEligibility").write[Boolean] and
          (JsPath \ "periods").write[List[TFCPeriod]]
    )(unlift(TFCEligibility.unapply))
}

case class TFCPeriod(
                      from: LocalDate,
                      until: LocalDate,
                      periodEligibility: Boolean,
                      children: List[Child]
                      )

object TFCPeriod extends CCFormat{

  implicit val periodFormat : Writes[TFCPeriod] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
        (JsPath \ "periodEligibility").write[Boolean] and
          (JsPath \ "children").write[List[Child]]
    )(unlift(TFCPeriod.unapply))
}
