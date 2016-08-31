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

package models.payload.eligibility.input.tfc

import models.payload.eligibility.input.{Claimant, Child}
import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import utils.CCFormat

/**
 * Created by user on 08/03/16.
 */

case class TFCPayload(
                       eligibility: TFCEligibility
                           )

object TFCPayload {
  implicit val request: Writes[TFCPayload] = (JsPath \ "payload").write[TFCEligibility].contramap { (payload : TFCPayload) => payload.eligibility}
}

case class TFCEligibility(
                    tfc: TFC
                    )

object TFCEligibility {
  implicit val request: Writes[TFCEligibility] = (JsPath \ "tfc").write[TFC].contramap { (eligibility : TFCEligibility) => eligibility.tfc}
}

case class TFC(
                from: LocalDate,
                numberOfPeriods: Short,
                claimants: List[Claimant],
                children: List[Child]
                )

object TFC extends CCFormat {
  implicit val tfc : Writes[TFC] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "numberOfPeriods").write[Short] and
      (JsPath \ "claimants").write[List[Claimant]] and
      (JsPath \ "children").write[List[Child]]
    )(unlift(TFC.unapply))
}
