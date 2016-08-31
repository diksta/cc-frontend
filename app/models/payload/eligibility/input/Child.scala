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
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import utils.CCFormat
import mappings.Periods
import play.api.libs.json.Writes._

/**
 * Created by user on 09/03/16.
 */
case class Child  (
                    id: Short,
                    name: Option[String],
                    childcareCost: BigDecimal = BigDecimal(0.00),
                    childcareCostPeriod: Periods.Period,
                    dob: LocalDate,
                    disability: Disability,
                    education: Option[Education]
                    )

object Child extends CCFormat {
  implicit val child : Writes[Child] = (
    (JsPath \ "id").write[Short] and
      (JsPath \ "name").writeNullable[String] and
      (JsPath \ "childcareCost").write[BigDecimal] and
      (JsPath \ "childcareCostPeriod").write[Periods.Period] and
      (JsPath \ "dob").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "disability").write[Disability] and
      (JsPath \ "education").writeNullable[Education]
    )(unlift(Child.unapply))
}

case class Education(
                      inEducation: Boolean = false,
                      startDate: LocalDate
                      )

object Education extends CCFormat {
  implicit val educationReads : Writes[Education] = (
    (JsPath \ "inEducation").write[Boolean] and
      (JsPath \ "startDate").write[LocalDate](jodaLocalDateWrites(datePattern))
    )((unlift(Education.unapply)))
}
