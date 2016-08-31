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

import mappings.Periods
import play.api.Logger
import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}

/**
 * Created by adamconder on 21/01/16.
 */
object PeriodFormatter {

  private def error(key: String, msg: String) = Left(List(new FormError(key, msg)))

  implicit val periodsFormatter = new Formatter[Periods.Period] {

    override def bind(key: String, data: Map[String, String]) : Either[Seq[FormError], Periods.Period] = {
      Logger.debug(s"PeriodFormatter.bind ")
      data.get(key).map { value =>
        try {
          Right(Periods.withName(value))
        } catch {
          case e : NoSuchElementException => error(key, "Please enter a valid time period.")
        }
      }.getOrElse(error(key, "No period type provided."))
    }

    override def unbind(key: String, value: Periods.Period) : Map[String, String] = {
      Logger.debug(s"PeriodFormatter.unbind ")
      Map(key -> value.toString())
    }

  }

  def period : Mapping[Periods.Period] = Forms.of[Periods.Period]

}
