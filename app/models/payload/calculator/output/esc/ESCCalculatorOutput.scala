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

package models.payload.calculator.output.esc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.CCFormat
import mappings.Periods

case class ESCCalculatorOutput(
                           from : LocalDate,
                           until : LocalDate,
                           totalSavings : Savings,
                           taxYears : List[ESCTaxYear]
                           )

object ESCCalculatorOutput extends CCFormat {
  implicit val escCalculationReads : Reads[ESCCalculatorOutput] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "totalSavings").read[Savings] and
          (JsPath \ "taxYears").read[List[ESCTaxYear]]
    )(ESCCalculatorOutput.apply _)
  }

case class ESCTaxYear(
                    from : LocalDate,
                    until : LocalDate,
                    totalSavings : Savings,
                    claimants : List[ESCOutputClaimant]
                    )

object ESCTaxYear extends CCFormat {
  implicit val TaxYearReads: Reads[ESCTaxYear] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "totalSavings").read[Savings] and
          (JsPath \ "claimants").read[List[ESCOutputClaimant]]
    )(ESCTaxYear.apply _)
  }

case class Savings(
                    totalSaving : BigDecimal = BigDecimal(0.00),
                    taxSaving: BigDecimal = BigDecimal(0.00),
                    niSaving : BigDecimal = BigDecimal(0.00)
                    )

object Savings extends CCFormat {
  implicit val SavingsReads : Reads[Savings] = (
    (JsPath \ "totalSaving").read[BigDecimal] and
      (JsPath \ "taxSaving").read[BigDecimal] and
        (JsPath \ "niSaving").read[BigDecimal]
    )(Savings.apply _)
  }

case class ESCOutputClaimant(
                     qualifying : Boolean = false,
                     eligibleMonthsInTaxYear : Int,
                     isPartner : Boolean = false,
                     income : Income,
                     elements : ClaimantElements,
                     escAmount : BigDecimal = BigDecimal(0.00),
                     escAmountPeriod : Periods.Period,
                     escStartDate : LocalDate,
                     savings : Savings,
                     maximumRelief : BigDecimal = BigDecimal(0.00),
                     maximumReliefPeriod : Periods.Period,
                     taxAndNIBeforeSacrifice : TaxAndNI,
                     taxAndNIAfterSacrifice : TaxAndNI
                     )

object ESCOutputClaimant extends CCFormat {
  implicit val claimantReads: Reads[ESCOutputClaimant] = (
    (JsPath \ "qualifying").read[Boolean] and
      (JsPath \ "eligibleMonthsInTaxYear").read[Int] and
        (JsPath \ "isPartner").read[Boolean] and
          (JsPath \ "income").read[Income] and
            (JsPath \ "elements").read[ClaimantElements] and
              (JsPath \ "escAmount").read[BigDecimal] and
                (JsPath \ "escAmountPeriod").read[Periods.Period] and
                  (JsPath \ "escStartDate").read[LocalDate](jodaLocalDateReads(datePattern)) and
                    (JsPath \ "savings").read[Savings] and
                      (JsPath \ "maximumRelief").read[BigDecimal] and
                        (JsPath \ "maximumReliefPeriod").read[Periods.Period] and
                          (JsPath \ "taxAndNIBeforeSacrifice").read[TaxAndNI] and
                            (JsPath \ "taxAndNIAfterSacrifice").read[TaxAndNI]
    )(ESCOutputClaimant.apply _)
  }

case class Income(
                   taxablePay: BigDecimal = BigDecimal(0.00),
                   gross: BigDecimal = BigDecimal(0.00),
                   taxCode: String = "",
                   niCategory: String = ""
                   )

object Income extends CCFormat {
  implicit val IncomeReads: Reads[Income]=(
    (JsPath \ "taxablePay").read[BigDecimal] and
      (JsPath \ "gross").read[BigDecimal] and
        (JsPath \ "taxCode").read[String] and
          (JsPath \ "niCategory").read[String]
    )(Income.apply _)
}

case class ClaimantElements(
    vouchers : Boolean = false
  )

object ClaimantElements extends CCFormat {
  implicit val ClaimantElementsReads: Format[ClaimantElements] = Json.format[ClaimantElements]
}

case class TaxAndNI(
  taxPaid : BigDecimal = BigDecimal(0.00),
  niPaid : BigDecimal = BigDecimal(0.00)
 )

object TaxAndNI extends CCFormat {
  implicit val taxAndNIReads: Reads[TaxAndNI] = (
    (JsPath \ "taxPaid").read[BigDecimal] and
      (JsPath \ "niPaid").read[BigDecimal]
    )(TaxAndNI.apply _)
}
