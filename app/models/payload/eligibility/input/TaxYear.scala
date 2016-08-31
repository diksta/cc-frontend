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

import org.joda.time.LocalDate
import play.api.libs.json.{Writes, JsPath}
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import utils.CCFormat

/**
 * Created by elsie on 10/03/16.
 */
case class TaxYear(
                    from: LocalDate,
                    until: LocalDate,
                    claimants: List[Claimant],
                    children: List[Child]
                    )
object TaxYear extends CCFormat {
  implicit val taxYearWrite: Writes[TaxYear] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "claimants").write[List[Claimant]] and
      (JsPath \ "children").write[List[Child]]
    )(unlift(TaxYear.unapply))
}
