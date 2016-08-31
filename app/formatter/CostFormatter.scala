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

package formatter

import config.ApplicationConfig
import play.api.Logger
import play.api.data.FormError
import play.api.data.format.{Formats, Formatter}
import play.api.i18n.Messages

/**
 * Created by adamconder on 21/01/16.
 */
object CostFormatter {

  val numberRegex = """^\-?[0-9]\d*(\.\d+)?$""".r

  def validateCostLowerThanLowerLimit(cost: BigDecimal): Boolean = {
    Logger.debug(s"CostFormatter.validateCostLowerThanLowerLimit ")
    cost < BigDecimal(ApplicationConfig.minimumChildCareCost)
  }

  def validateCostGreaterThanUpperLimit(cost: BigDecimal): Boolean = {
    Logger.debug(s"CostFormatter.validateCostGreaterThanUpperLimit ")
    cost > BigDecimal(ApplicationConfig.maximumChildCareCost)
  }

  class CCCostFormatter(precision: Option[(Int, Int)]) extends Formatter[BigDecimal] {

    def validateTwoDecimalPoint(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      Logger.debug(s"CostFormatter.validateTwoDecimalPoint ")
      val errorMessage = Messages("cc.child.details.error.amount.decimal.limit")

      Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[BigDecimal].either {
          val bd = BigDecimal(s)
          precision.map({
            case (p, ps) =>
              if (bd.precision - bd.scale > p - ps) {
                Left(List(FormError(key, errorMessage, Nil)))
              }
              bd.setScale(ps)
          }).getOrElse(bd)
        }.left.map { e =>
          Seq(
            precision match {
              case Some((p, ss)) =>
                FormError(key,errorMessage, Nil)
              case None =>
                FormError(key, errorMessage, Nil)
            }
          )
        }
      }
    }

    override val format = Some(("format.real", Nil))

    override def bind(key: String, data: Map[String, String]) = {
      Logger.debug(s"CostFormatter.bind ")
      val cost = data.getOrElse(key, "0.00")

      val validateTwoDecimalPlaces = validateTwoDecimalPoint(key, data)

      // if provided null or the cost is empty
      if (cost == null || cost.isEmpty) {
        Left(List(FormError(key, Messages("cc.childcare.cost.error.not.a.number"), Nil)))
      }
      // if the string does not match the regex
      else if (!cost.matches(numberRegex.toString())) {
        Left(List(FormError(key, Messages("cc.childcare.cost.error.not.a.number"), Nil)))
      }
      // if the validation of the decimal points failed
      else if(validateTwoDecimalPlaces.isLeft) {
        // get the correct error
        val error = validateTwoDecimalPlaces.left.get.head
        Left(List(FormError(key, error.message, Nil)))
      }
      // if the cost is lower than the lower limit
      else if (cost.matches(numberRegex.toString()) && validateCostLowerThanLowerLimit(BigDecimal(cost))) {
        Left(List(FormError(key, Messages("cc.childcare.cost.error.not.a.number"), Nil)))
      }
      // if the cost is greater than upper limit
      else if (cost.matches(numberRegex.toString()) && validateCostGreaterThanUpperLimit(BigDecimal(cost))) {
        Left(List(FormError(key, Messages("cc.childcare.cost.error.not.a.number"), Nil)))
      }
      else {
        // the validation has passed return the BigDecimal
        Right(BigDecimal(cost))
      }
    }

    def unbind(key: String, value: BigDecimal) : Map[String, String] = Map(key -> precision.map({ p => value.setScale(p._2) }).getOrElse(value).toString)
  }

  def costFormatter(precision: Option[(Int, Int)]): Formatter[BigDecimal] = new CCCostFormatter(precision)
  
}
