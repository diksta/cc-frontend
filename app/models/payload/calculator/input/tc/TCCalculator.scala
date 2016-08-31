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

package models.payload.calculator.input.tc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{JsPath, Writes}
import utils.CCFormat
import mappings.Periods

/**
 * Created by elsie on 11/03/16.
 */


case class TCEligibility(
                           taxYears : List[TaxYear],
                           proRataEnd : LocalDate
                           )


object TCEligibility extends CCFormat{
  implicit val tcEligibilityFormat: Writes[TCEligibility] = (
    (JsPath \ "taxYears").write[List[TaxYear]] and
      (JsPath \ "proRataEnd").write[LocalDate](jodaLocalDateWrites(datePattern))
    )(unlift(TCEligibility.unapply))
}


case class TaxYear(
                    from: LocalDate,
                    until: LocalDate,
                    houseHoldIncome : BigDecimal,
                    periods: List[Period]
                    )

object TaxYear extends CCFormat {
  implicit val taxYearFormat: Writes[TaxYear] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
        (JsPath \ "houseHoldIncome").write[BigDecimal] and
          (JsPath \ "periods").write[List[Period]]
    )(unlift(TaxYear.unapply))
}

case class Period(
                   from: LocalDate,
                   until: LocalDate,
                   householdElements: HouseHoldElements,
                   claimants: List[Claimant],
                   children: List[Child]
                   )

object Period extends CCFormat {
  implicit val periodFormat: Writes[Period] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "householdElements").write[HouseHoldElements] and
      (JsPath \ "claimants").write[List[Claimant]] and
      (JsPath \ "children").write[List[Child]]
    )(unlift(Period.unapply))
}

case class HouseHoldElements(basic: Boolean = false,
                             hours30: Boolean = false,
                             childcare: Boolean = false,
                             loneParent: Boolean = false,
                             secondParent: Boolean = false,
                             family: Boolean = false
                              )

  object HouseHoldElements {
    implicit val householdElementsFormat: Writes[HouseHoldElements] = (
      (JsPath \ "basic").write[Boolean] and
        (JsPath \ "hours30").write[Boolean] and
        (JsPath \ "childcare").write[Boolean] and
        (JsPath \ "loneParent").write[Boolean] and
        (JsPath \ "secondParent").write[Boolean] and
        (JsPath \ "family").write[Boolean]
      )(unlift(HouseHoldElements.unapply))
  }

case class Claimant(qualifying: Boolean,
                    isPartner: Boolean,
                    claimantElements: ClaimantDisability,
                    doesNotTaper : Boolean = false,
                    failures: Option[List[String]])

  object Claimant {
    implicit val claimantFormat: Writes[Claimant] = (
      (JsPath \ "qualifying").write[Boolean] and
        (JsPath \ "isPartner").write[Boolean] and
          (JsPath \ "claimantElements").write[ClaimantDisability] and
            (JsPath \ "doesNotTaper").write[Boolean] and
              (JsPath \ "failures").writeNullable[List[String]]
      )(unlift(Claimant.unapply))
  }

case class ClaimantDisability(disability: Boolean = false,
                              severeDisability: Boolean = false
                               )

  object ClaimantDisability {
    implicit val claimantDisabilityFormat: Writes[ClaimantDisability] = (
      (JsPath \ "disability").write[Boolean] and
        (JsPath \ "severeDisability").write[Boolean]
      )(unlift(ClaimantDisability.unapply))
  }

  case class Child(id: Short,
                   name : String,
                   qualifying: Boolean = false,
                   childcareCost : BigDecimal,
                   childcareCostPeriod: Periods.Period,
                   childElements: ChildElements)

  object Child {
    implicit val childDisabilityFormat: Writes[Child] = (
      (JsPath \ "id").write[Short] and
        (JsPath \ "name").write[String] and
          (JsPath \ "qualifying").write[Boolean] and
            (JsPath \ "childcareCost").write[BigDecimal] and
              (JsPath \ "childcareCostPeriod").write[Periods.Period] and
                (JsPath \ "childElements").write[ChildElements]
//                  (JsPath \ "failures").writeNullable[List[String]]
      )(unlift(Child.unapply))
  }

  case class ChildElements(child: Boolean = false,
                           youngAdult: Boolean = false,
                           disability: Boolean = false,
                           severeDisability: Boolean = false,
                           childcare: Boolean = false
                            )

  object ChildElements {
    implicit val ChildElementsFormat: Writes[ChildElements] = (
      (JsPath \ "child").write[Boolean] and
        (JsPath \ "youngAdult").write[Boolean] and
          (JsPath \ "disability").write[Boolean] and
            (JsPath \ "severeDisability").write[Boolean] and
              (JsPath \ "childcare").write[Boolean]
      )(unlift(ChildElements.unapply))
}
