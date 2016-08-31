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

package mappings

import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.{Format, Reads, Writes}
import utils.EnumUtils

/**
 * Created by adamconder on 21/01/16.
 */

object Periods extends Enumeration {

  type Period = Value

  val Weekly = Value(0, "Week")
  val Fortnightly = Value(1, "Fortnight")
  val Monthly = Value(2, "Month")
  val Quarterly = Value(3, "3 month")
  val Yearly = Value(4, "Year")
  val INVALID = Value(5, "INVALID")

  val enumReads: Reads[Period] = EnumUtils.enumReads(Periods)

  val enumWrites: Writes[Period] = EnumUtils.enumWrites

  implicit def enumFormats : Format[Period] = EnumUtils.enumFormat(Periods)

  def toString(period: Value) : String = {
    Logger.debug(s"Periods.toString ")
    period match {
      case Weekly => Messages("cc.period.weekly")
      case Fortnightly => Messages("cc.period.fortnightly")
      case Monthly => Messages("cc.period.monthly")
      case Quarterly => Messages("cc.period.3monthly")
      case Yearly => Messages("cc.period.yearly")
      case _ => Messages("cc.period.invalid")
    }
  }

  def toPeriod(period: String) : Value = {
    Logger.debug(s"Periods.toPeriod ")
    period match {
      case "monthly" => Monthly
      case "3-monthly" => Quarterly
      case _ =>  INVALID
    }
  }

}
