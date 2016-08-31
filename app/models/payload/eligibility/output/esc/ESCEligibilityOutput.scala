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

package models.payload.eligibility.output.esc

import org.joda.time.LocalDate
import play.api.libs.json.{Reads, JsPath}
import play.api.libs.json.Reads._
import utils.CCFormat
import play.api.libs.functional.syntax._

/**
 * Created by user on 18/03/16.
 */
case class ESCEligibilityOutput (
                                  taxYears: List[TaxYear]
                                  )

object ESCEligibilityOutput {
  implicit val eligibilityOutputFormat: Reads[ESCEligibilityOutput] =
    (JsPath \ "taxYears").read[List[TaxYear]].map { eligibility => ESCEligibilityOutput(eligibility)}
}

case class TaxYear(
                    from: LocalDate,
                    until: LocalDate,
                    periods: List[ESCPeriod]
                    )

object TaxYear extends CCFormat{
  implicit val taxYearReads: Reads[TaxYear] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "periods").read[List[ESCPeriod]]
    )(TaxYear.apply _)
}

case class ESCPeriod(
                      from: LocalDate,
                      until: LocalDate,
                      claimants: List[ESCOutputClaimant],
                      children: List[ESCOutputChild]
                      )

object ESCPeriod extends CCFormat{
  implicit val periodReads : Reads[ESCPeriod] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "claimants").read[List[ESCOutputClaimant]] and
          (JsPath \ "children").read[List[ESCOutputChild]]
    )(ESCPeriod.apply _)
}

case class ESCOutputClaimant(
                           qualifying: Boolean = false,
                           isPartner: Boolean = false,
                           eligibleMonthsInPeriod: Int = 0,
                           elements: ClaimantElements,
                           failures: List[String]
                           )

//escAmount can be a voucher amount, childcare bursary amount or directly contracted amount
object ESCOutputClaimant extends CCFormat {
  implicit val claimantReads : Reads[ESCOutputClaimant] = (
    (JsPath \ "qualifying").read[Boolean] and
      (JsPath \ "isPartner").read[Boolean] and
        (JsPath \ "eligibleMonthsInPeriod").read[Int] and
          (JsPath \ "elements").read[ClaimantElements] and
            (JsPath \ "failures").read[List[String]]
    )(ESCOutputClaimant.apply _)
}

case class ClaimantElements(
                             // claimants qualification is determined by employer providing esc and children's qualification (if there is at least 1 qualifying child)
                             vouchers : Boolean = false
                             )

object ClaimantElements {
  implicit val claimantReads : Reads[ClaimantElements] = (JsPath \ "vouchers").read[Boolean].map { (elements => ClaimantElements(elements)) }
}

case class ESCOutputChild(
                        id: Short,
                        name: Option[String],
                        qualifying: Boolean = false,
                        failures: List[String]
                        )

object ESCOutputChild {
  implicit val childReads : Reads[ESCOutputChild] = (
    (JsPath \ "id").read[Short] and
      (JsPath \ "name").readNullable[String] and
        (JsPath \ "qualifying").read[Boolean] and
          (JsPath \ "failures").read[List[String]]
    )(ESCOutputChild.apply _)
}
