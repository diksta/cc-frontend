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

package models.payload.eligibility.input.tc

import models.payload.eligibility.input.{TaxYear, Child, Claimant}
import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import utils.CCFormat

/**
 * Created by user on 09/03/16.
 */

case class TCPayload(
                       eligibility: TCEligibility
                       )

object TCPayload {
  implicit val request: Writes[TCPayload] = (JsPath \ "payload").write[TCEligibility].contramap { (payload : TCPayload) => payload.eligibility}
}

case class TCEligibility(
                          taxYears: List[TaxYear]
                           )

object TCEligibility {
  implicit val request: Writes[TCEligibility] = (JsPath \ "taxYears").write[List[TaxYear]].contramap { (eligibility : TCEligibility) => eligibility.taxYears}
}
