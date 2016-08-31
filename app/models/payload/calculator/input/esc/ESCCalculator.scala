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

package models.payload.calculator.input.esc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json.{JsPath, Writes}
import utils.CCFormat
import mappings.Periods

/**
 * Created by elsie on 11/03/16.
 */

case class ESCEligibility(
                          escTaxYear: List[ESCTaxYear]
                          )


object ESCEligibility {
  implicit val EligibilityWrites: Writes[ESCEligibility] = (JsPath \ "taxYears").write[List[ESCTaxYear]].contramap { (escEligibility: ESCEligibility) => escEligibility.escTaxYear }
}

case class ESCTaxYear(
                    startDate: LocalDate,
                    endDate: LocalDate,
                    periods: List[ESCPeriod]
                    )

object ESCTaxYear extends CCFormat {
  implicit val escTaxYearFormat: Writes[ESCTaxYear] = (
      (JsPath \ "startDate").write[LocalDate](jodaLocalDateWrites(datePattern)) and
        (JsPath \ "endDate").write[LocalDate](jodaLocalDateWrites(datePattern)) and
          (JsPath \ "periods").write[List[ESCPeriod]]
    )(unlift(ESCTaxYear.unapply))
}

case class ESCPeriod(
                      from: LocalDate,
                      until: LocalDate,
                      claimants: List[ESCClaimant]
                      )

object ESCPeriod extends CCFormat {
  implicit val escPeriodFormat: Writes[ESCPeriod] = (
    (JsPath \ "from").write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ "until").write[LocalDate](jodaLocalDateWrites(datePattern)) and
        (JsPath \ "claimants").write[List[ESCClaimant]]
    )(unlift(ESCPeriod.unapply))
}

case class ESCClaimant (
                        qualifying: Boolean,
                        isPartner: Boolean,
                        eligibleMonthsInPeriod: Int,
                        income: ESCIncome,
                        elements: ESCElements,
                        escStartDate: LocalDate,
                        escAmount: BigDecimal,
                        escAmountPeriod: Periods.Period
                         )

object ESCClaimant extends CCFormat{
  implicit val escClaimantFormat: Writes[ESCClaimant] = (
    (JsPath \ "qualifying").write[Boolean] and
      (JsPath \ "isPartner").write[Boolean] and
        (JsPath \ "eligibleMonthsInPeriod").write[Int] and
          (JsPath \ "income").write[ESCIncome] and
            (JsPath \ "elements").write[ESCElements] and
              (JsPath \ "escStartDate").write[LocalDate](jodaLocalDateWrites(datePattern)) and
                (JsPath \ "escAmount").write[BigDecimal] and
                  (JsPath \ "escAmountPeriod").write[Periods.Period]
    )(unlift(ESCClaimant.unapply))
}

case class ESCIncome (
                       taxablePay: BigDecimal,
                       gross: BigDecimal,
                       taxCode: String,
                       niCategory: String
                       )

object ESCIncome {
  implicit val escIncomeFormat: Writes[ESCIncome] = (
    (JsPath \ "taxablePay").write[BigDecimal] and
      (JsPath \ "gross").write[BigDecimal] and
        (JsPath \ "taxCode").write[String] and
          (JsPath \ "niCategory").write[String]
    )(unlift(ESCIncome.unapply))
}

case class ESCElements (
                         vouchers: Boolean = true
                         )

object ESCElements {
  implicit val ESCElementsWrites: Writes[ESCElements] = (JsPath \ "vouchers").write[Boolean].contramap { (escElementsEligibility: ESCElements) => escElementsEligibility.vouchers}
}
