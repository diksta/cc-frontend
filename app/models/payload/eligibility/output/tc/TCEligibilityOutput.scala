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

package models.payload.eligibility.output.tc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.CCFormat
import mappings.Periods

case class TCEligibilityOutput (
                               eligible: Boolean = false,
                               taxYears: List[TaxYear]
                                 )

object TCEligibilityOutput {
  implicit val tcEligibilityOutputReads : Reads[TCEligibilityOutput] = (
    (JsPath \ "eligible").read[Boolean] and
      (JsPath \ "taxYears").read[List[TaxYear]]
    )(TCEligibilityOutput.apply _)
}

case class TaxYear(
                    from: LocalDate,
                    until: LocalDate,
                    houseHoldIncome: BigDecimal,
                    periods: List[TCPeriod]
                    )

object TaxYear extends CCFormat {
  implicit val taxYearReads: Reads[TaxYear] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "houseHoldIncome").read[BigDecimal] and
          (JsPath \ "periods").read[List[TCPeriod]]
    )(TaxYear.apply _)
}

case class TCPeriod(
                     from: LocalDate,
                     until: LocalDate,
                     householdElements: HouseholdElements,
                     claimants: List[TCOutputClaimant],
                     children: List[TCOutputChild]
                     )

object TCPeriod extends CCFormat {
  implicit val tcPeriodReads: Reads[TCPeriod] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "householdElements").read[HouseholdElements] and
          (JsPath \ "claimants").read[List[TCOutputClaimant]] and
            (JsPath \ "children").read[List[TCOutputChild]]
    )(TCPeriod.apply _)
}

case class HouseholdElements(
                              basic: Boolean = false,
                              hours30: Boolean = false,
                              childcare: Boolean = false,
                              loneParent: Boolean = false,
                              secondParent: Boolean = false,
                              family: Boolean = false
                              )

object HouseholdElements {
  implicit val householdElementsReads: Reads[HouseholdElements] = (
    (JsPath \ "basic").read[Boolean] and
      (JsPath \ "hours30").read[Boolean] and
        (JsPath \ "childcare").read[Boolean] and
          (JsPath \ "loneParent").read[Boolean] and
            (JsPath \ "secondParent").read[Boolean] and
              (JsPath \ "family").read[Boolean]
    )(HouseholdElements.apply _)
}

case class TCOutputClaimant(
                           qualifying: Boolean = false,
                           isPartner: Boolean = false,
                           claimantDisability: ClaimantDisability,
                           failures: List[String]
                           )

object TCOutputClaimant {
  implicit val outputClaimantReads: Reads[TCOutputClaimant] = (
    (JsPath \ "qualifying").read[Boolean] and
      (JsPath \ "isPartner").read[Boolean] and
        (JsPath \ "claimantDisability").read[ClaimantDisability] and
          (JsPath \ "failures").read[List[String]]
    )(TCOutputClaimant.apply _)
}

case class ClaimantDisability(
                               disability: Boolean,
                               severeDisability: Boolean
                               )

object ClaimantDisability {
  implicit val claimantElementReads : Reads[ClaimantDisability] = (
    (JsPath \ "disability").read[Boolean] and
      (JsPath \ "severeDisability").read[Boolean]
    )(ClaimantDisability.apply _)
}

case class TCOutputChild(
                        id: Short,
                        name: Option[String],
                        childcareCost: BigDecimal = BigDecimal(0.00),
                        childcareCostPeriod: Periods.Period,
                        qualifying: Boolean = false,
                        childElements: ChildElements,
                        failures: List[String]
                        )

object TCOutputChild {
  implicit val childReads : Reads[TCOutputChild] = (
    (JsPath \ "id").read[Short] and
      (JsPath \ "name").readNullable[String] and
        (JsPath \ "childcareCost").read[BigDecimal] and
          (JsPath \ "childcareCostPeriod").read[Periods.Period] and
            (JsPath \ "qualifying").read[Boolean] and
              (JsPath \ "childElements").read[ChildElements] and
                (JsPath \ "failures").read[List[String]]
    )(TCOutputChild.apply _)
}

case class ChildElements(
                          child: Boolean = false,
                          youngAdult: Boolean = false,
                          disability: Boolean = false,
                          severeDisability: Boolean = false,
                          childcare: Boolean = false
                          )

object ChildElements {
  implicit val childElementsReads : Reads[ChildElements] = (
    (JsPath \ "child").read[Boolean] and
      (JsPath \ "youngAdult").read[Boolean] and
        (JsPath \ "disability").read[Boolean] and
          (JsPath \ "severeDisability").read[Boolean] and
            (JsPath \ "childcare").read[Boolean]
    )(ChildElements.apply _)
}
