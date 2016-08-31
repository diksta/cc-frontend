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

package models.payload.calculator.output.tfc

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, JsPath}
import utils.CCFormat

case class TFCCalculatorOutput(
                           from: LocalDate,
                           until: LocalDate,
                           householdContribution: Contribution,
                           numberOfPeriods: Short,
                           periods: List[TFCPeriod]
                           )

object TFCCalculatorOutput extends CCFormat {
  implicit val tfcCalculationReads : Reads[TFCCalculatorOutput] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
         (JsPath \ "householdContribution").read[Contribution] and
          (JsPath \ "numberOfPeriods").read[Short] and
           (JsPath \ "periods").read[List[TFCPeriod]]
    )(TFCCalculatorOutput.apply _)
}

case class TFCPeriod(
                      from: LocalDate,
                      until: LocalDate,
                      periodContribution : Contribution,
                      children: List[TFCOutputChild]
                      )

object TFCPeriod extends CCFormat{
  implicit val periodReads : Reads[TFCPeriod] = (
    (JsPath \ "from").read[LocalDate](jodaLocalDateReads(datePattern)) and
      (JsPath \ "until").read[LocalDate](jodaLocalDateReads(datePattern)) and
        (JsPath \ "periodContribution").read[Contribution] and
          (JsPath \ "children").read[List[TFCOutputChild]]
    )(TFCPeriod.apply _)
}

case class TFCOutputChild(
                        id: Short,
                        name: Option[String],
                        childCareCost : BigDecimal = BigDecimal(0.00),
                        childContribution : Contribution,
                        timeToMaximizeTopUp : Short,
                        failures: List[String]
                        )

object TFCOutputChild extends CCFormat {
  implicit val childReads : Reads[TFCOutputChild] = (
    (JsPath \ "id").read[Short] and
      (JsPath \ "name").readNullable[String] and
          (JsPath \ "childCareCost").read[BigDecimal] and
              (JsPath \ "childContribution").read[Contribution] and
                (JsPath \ "timeToMaximizeTopUp").read[Short] and
                  (JsPath \ "failures").read[List[String]]
    )(TFCOutputChild.apply _)
}

case class Contribution (
                          parent : BigDecimal = BigDecimal(0.00),
                          government : BigDecimal = BigDecimal(0.00),
                          totalChildCareSpend : BigDecimal = BigDecimal(0.00)
                          )

object Contribution {
  implicit val contributionReads : Reads[Contribution] = (
  (JsPath \ "parent").read[BigDecimal] and
    (JsPath \ "government").read[BigDecimal] and
      (JsPath \ "totalChildCareSpend").read[BigDecimal]
    )(Contribution.apply _)
}
