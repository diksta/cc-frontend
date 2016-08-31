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

package models.payload.calculator.output.tc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

object TCCalculatorOutput extends utils.CCFormat {
  implicit val TCCalculationReads: Reads[TCCalculatorOutput] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "proRataEnd").readNullable[LocalDate](jodaLocalDateReads(datePattern)) and
          (JsPath \ "totalAwardAmount").read[BigDecimal] and
            (JsPath \ "totalAwardProRataAmount").read[BigDecimal] and
              (JsPath \ "houseHoldAdviceAmount").read[BigDecimal] and
               (JsPath \ "totalHouseHoldAdviceProRataAmount").read[BigDecimal] and
                  (JsPath \ "taxYears").read[List[TCTaxYear]]
    )(TCCalculatorOutput.apply _)
}

case class TCCalculatorOutput(from: LocalDate,
                         until: LocalDate,
                         proRataEnd: Option[LocalDate] = None,
                         totalAwardAmount: BigDecimal = 0.00,
                         totalAwardProRataAmount: BigDecimal = 0.00,
                         houseHoldAdviceAmount: BigDecimal = 0.00,
                         totalHouseHoldAdviceProRataAmount: BigDecimal = 0.00,
                         taxYears: List[TCTaxYear]
                          )

object TCTaxYear extends utils.CCFormat {
  implicit val TaxYearReads: Reads[TCTaxYear] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "proRataEnd").readNullable[LocalDate](jodaLocalDateReads(datePattern)) and
          (JsPath \ "taxYearAwardAmount").read[BigDecimal] and
            (JsPath \ "taxYearAwardProRataAmount").read[BigDecimal] and
              (JsPath \ "taxYearAdviceAmount").read[BigDecimal] and
                (JsPath \ "taxYearAdviceProRataAmount").read[BigDecimal] and
                  (JsPath \ "periods").read[List[TCPeriod]]
    )(TCTaxYear.apply _)
}

case class TCTaxYear(
                    from: LocalDate,
                    until: LocalDate,
                    proRataEnd: Option[LocalDate] = None,
                    taxYearAwardAmount: BigDecimal = BigDecimal(0.00),
                    taxYearAwardProRataAmount: BigDecimal = BigDecimal(0.00),
                    taxYearAdviceAmount: BigDecimal = BigDecimal(0.00),
                    taxYearAdviceProRataAmount: BigDecimal = BigDecimal(0.00),
                    periods: List[TCPeriod]
                    )

object TCPeriod extends utils.CCFormat {
  implicit val ElementReads: Reads[TCPeriod] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "periodNetAmount").read[BigDecimal] and
          (JsPath \ "periodAdviceAmount").read[BigDecimal] and
            (JsPath \ "elements").read[Elements]
    )(TCPeriod.apply _)
}

case class TCPeriod(from: LocalDate,
                  until: LocalDate,
                  periodNetAmount: BigDecimal = 0.00,
                  periodAdviceAmount: BigDecimal = 0.00,
                  elements: Elements)


object Elements {
  implicit val ElementsReads: Reads[Elements] = (
      (JsPath \ "wtcWorkElement").read[Element] and
        (JsPath \ "wtcChildcareElement").read[Element] and
         (JsPath \ "ctcIndividualElement").read[Element] and
           (JsPath \ "ctcFamilyElement").read[Element]
    )(Elements.apply _)
}

case class Elements(wtcWorkElement: Element,
                     wtcChildcareElement: Element,
                     ctcIndividualElement: Element,
                     ctcFamilyElement: Element)

object Element {
  implicit val ElementReads: Reads[Element] = (
    (JsPath \ "netAmount").read[BigDecimal] and
      (JsPath \ "maximumAmount").read[BigDecimal] and
        (JsPath \ "taperAmount").read[BigDecimal]
    )(Element.apply _)
}

case class Element(netAmount: BigDecimal = 0.00,
                   maximumAmount: BigDecimal = 0.00,
                   taperAmount: BigDecimal = 0.00)
