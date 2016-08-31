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

package models.claimant

import play.api.libs.json.Json

/**
 * Created by adamconder on 04/02/2016.
 */
case class Claimant(
                   id: Short, // default to 1 for parent and 2 for partner
                   disability: Disability,
                   previousIncome: Option[Income] = None,
                   currentIncome : Option[Income] = None,
                   hours : Option[BigDecimal] = None,
                   whereDoYouLive : Option[String] = None,
                   doYouLiveWithPartner : Option[Boolean] = None,
                   escVouchersAvailable : Option[String] = None
                     )

object Claimant {
  implicit val formats = Json.format[Claimant]
}
