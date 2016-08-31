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

package models.payload.eligibility.output.tfc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import utils.CCFormat

/**
 * Created by user on 18/03/16.
 */
case class TFCEligibilityOutput(
                                 from: LocalDate,
                                 until: LocalDate,
                                 householdEligibility : Boolean = false,
                                 periods: List[TFCPeriod]
                                 )

object TFCEligibilityOutput extends CCFormat {
  implicit val tfcOutputEligibility: Reads[TFCEligibilityOutput] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "householdEligibility").read[Boolean] and
          (JsPath \ "periods").read[List[TFCPeriod]]
    )(TFCEligibilityOutput.apply _)
}

case class TFCPeriod(
                      from: LocalDate,
                      until: LocalDate,
                      periodEligibility: Boolean = false,
                      claimants: List[TFCOutputClaimant],
                      children: List[TFCOutputChild]
                      )

object TFCPeriod extends CCFormat {
  implicit val periodReads : Reads[TFCPeriod] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "periodEligibility").read[Boolean] and
          (JsPath \ "claimants").read[List[TFCOutputClaimant]] and
            (JsPath \ "children").read[List[TFCOutputChild]]
    )(TFCPeriod.apply _)
}

case class TFCOutputClaimant(
                           qualifying: Boolean = false,
                           isPartner: Boolean = false,
                           failures: List[String]
                           )

object TFCOutputClaimant extends CCFormat {
  implicit val claimantReads: Reads[TFCOutputClaimant] = (
    (JsPath \ "qualifying").read[Boolean] and
      (JsPath \ "isPartner").read[Boolean] and
        (JsPath \ "failures").read[List[String]]
    )(TFCOutputClaimant.apply _)
}

case class TFCOutputChild(
                        id: Short,
                        name: Option[String],
                        qualifying: Boolean = false,
                        from: Option[LocalDate],
                        until: Option[LocalDate],
                        failures: List[String]
                        )

object TFCOutputChild extends CCFormat {
  implicit val childReads : Reads[TFCOutputChild] = (
    (JsPath \ "id").read[Short] and
      (JsPath \ "name").readNullable[String] and
        (JsPath \ "qualifying").read[Boolean] and
          ((JsPath \ "from").readNullable[LocalDate](jodaLocalDateReads(datePattern)) or Reads.optionWithNull(jodaLocalDateReads(datePattern))) and
              ((JsPath \ "until").readNullable[LocalDate](jodaLocalDateReads(datePattern)) or Reads.optionWithNull(jodaLocalDateReads(datePattern))) and
              (JsPath \ "failures").read[List[String]]
    )(TFCOutputChild.apply _)
}
