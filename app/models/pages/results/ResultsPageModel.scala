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

package models.pages.results

import play.api.libs.json.Json

/**
 * Created by ben on 27/04/16.
 */
case class ResultsPageModel (
                              annualCost : Int,
                              schemes : Seq[Scheme],
                              tcAmountByUser : Int,
                              ucAmountByUser : Int,
                              tfcEligibility : Boolean,
                              escEligibility : Boolean,
                              freeEntitlement: Option[FreeEntitlementPageModel] = None,
                              escVouchersAvailable : EscVouchersAvailablePageModel
                              )

object ResultsPageModel {
  implicit val schemeFormat = Json.format[Scheme]
  implicit val formats = Json.format[ResultsPageModel]
}

case class Scheme(val name: String, val amount: Int)