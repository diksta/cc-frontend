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

package controllers.manager

import java.text.NumberFormat
import java.util.{Calendar, Locale, TimeZone}

import config.ApplicationConfig
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, LocalDate}
import play.api.Logger
import play.api.i18n.Messages

import scala.annotation.tailrec

/**
 * Created by adamconder on 27/11/14.
 */
trait HelperManager {

  private[manager] val gdsDate = DateTimeFormat.forPattern("d M YYYY").withZone(DateTimeZone.forID("Europe/London"))

  def formatGDSDate(date: LocalDate) = gdsDate.print(date)

  def convertToCurrency(amount: BigDecimal) : String = {
    Logger.debug(s"HelperManager.convertToCurrency ")
    val formatter : NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    formatter.format(amount).trim
  }

  def concatenateNames(names: List[String]) : String = {
    Logger.debug(s"HelperManager.concatenateNames ")
    @tailrec
    def loop(names : List[String], acc: String) : String = {
      names match {
        case Nil =>
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          acc
        // $COVERAGE-ON
        case head :: tail =>
          loop(tail, acc + head.capitalize + (if (tail.length==0) { " " } else if(tail.length == 1) { " and " } else if (tail.length > 1) { ", " }) )
      }
    }
    loop(names, "")
  }

  def childsLowerAgeLimit(date: LocalDate) : LocalDate = {
    Logger.debug(s"HelperManager.childsLowerAgeLimit ")
    setCalendar(date, ApplicationConfig.childLowerBoundYears, days = true)
  }

  def childsUpperAgeLimit(date: LocalDate) : LocalDate = {
    Logger.debug(s"HelperManager.childsUpperAgeLimit ")
    setCalendar(date, ApplicationConfig.childUpperBoundYears, days = false)
  }

  def setCalendar(date: LocalDate, years: Int, days: Boolean): LocalDate = {
    Logger.debug(s"HelperManager.setCalendar ")
    val calendar : Calendar = Calendar.getInstance()
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"))
    calendar.setTime(date.toDate)
    if(days) calendar.add(Calendar.DAY_OF_YEAR, ApplicationConfig.childLowerBoundDays)
    calendar.add(Calendar.YEAR, years)
    LocalDate.fromDateFields(calendar.getTime)
  }

  def age(dob: Option[LocalDate], currentDate: LocalDate = LocalDate.now()) : Int = {
    Logger.debug(s"HelperManager.age ")
    val dobCalendar : Calendar = Calendar.getInstance()
    dob match {
      case Some(d) =>
        dobCalendar.setTime(d.toDate)
      case _ => throw new IllegalArgumentException(Messages("cc.child.details.invalid.date.of.birth"))
    }

    val today = Calendar.getInstance()
    today.setTime(currentDate.toDate)

    if (dobCalendar.after(today)) {
      -1
    } else {
      val age: Int = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
      if (today.get(Calendar.MONTH) < dobCalendar.get(Calendar.MONTH)) {
        age - 1
      } else if (today.get(Calendar.MONTH) == dobCalendar.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < dobCalendar.get(Calendar.DAY_OF_MONTH)) {
        age - 1
      } else {
        age
      }
    }
  }


  def september1stForDate(today: LocalDate = LocalDate.now()) : LocalDate = {
    Logger.debug(s"HelperManager.september1stForDate ")
    val currentYear = determineTaxYearFromNow(today)
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(Calendar.MONTH, Calendar.SEPTEMBER)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.YEAR, currentYear)
    val september1 = calendar.getTime
    LocalDate.fromDateFields(september1)
  }

  def september1stFollowingChildBirthday(childBirthday: LocalDate) : LocalDate = {
    Logger.debug(s"HelperManager.september1stFollowingChildBirthday ")
    // plot the child's birthday (e.g. 16th birthday) on the calendar
    val childBirthdayCalendar = Calendar.getInstance()
    childBirthdayCalendar.clear()
    childBirthdayCalendar.setTime(childBirthday.toDate)

    // determine 1st september for the child's birthday (current year)
    // if their birthday is after september then we have to go to the following year
    val septemberCalendar = Calendar.getInstance()
    septemberCalendar.clear()
    septemberCalendar.setTime(childBirthday.toDate)
    septemberCalendar.set(Calendar.MONTH, Calendar.SEPTEMBER)
    septemberCalendar.set(Calendar.DAY_OF_MONTH, 1)

    // if 16th birthday is after the determined 1st september then we need to add a year to the following september
    if (childBirthdayCalendar.compareTo(septemberCalendar) > 0 || childBirthdayCalendar.compareTo(septemberCalendar) == 0) {
      septemberCalendar.add(Calendar.YEAR, 1)
    }

    val september1 = septemberCalendar.getTime
    LocalDate.fromDateFields(september1)
  }

  def determineTaxYearFromNow(from: LocalDate) : Int = {
    Logger.debug(s"HelperManager.determineTaxYearFromNow ")
    val currentCalendar = Calendar.getInstance()
    currentCalendar.clear()
    currentCalendar.setTime(from.toDate)
    val periodYear = currentCalendar.get(Calendar.YEAR)
    val periodStart = from.toDate

    val januaryCalendar = Calendar.getInstance()
    januaryCalendar.clear()
    januaryCalendar.set(Calendar.YEAR, periodYear)
    januaryCalendar.set(Calendar.MONTH, Calendar.JANUARY)
    januaryCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val january1st = januaryCalendar.getTime

    val aprilCalendar = Calendar.getInstance()
    aprilCalendar.clear()
    aprilCalendar.set(Calendar.YEAR, periodYear)
    aprilCalendar.set(Calendar.MONTH, Calendar.APRIL)
    aprilCalendar.set(Calendar.DAY_OF_MONTH, 5)
    val april5th = aprilCalendar.getTime

    if ((periodStart.compareTo(january1st) == 0 || periodStart.after(january1st)) && (periodStart.before(april5th) || periodStart.compareTo(april5th) == 0)) {
      periodYear-1
    } else {
      periodYear
    }
  }

  def determineLastTaxYear(from: LocalDate) : (Int,Int) = {
    Logger.debug(s"HelperManager.determineLastTaxYear ")
    val currentCalendar = Calendar.getInstance()
    currentCalendar.clear()
    currentCalendar.setTime(from.toDate)
    val periodYear = currentCalendar.get(Calendar.YEAR)
    val periodStart = from.toDate

    val januaryCalendar = Calendar.getInstance()
    januaryCalendar.clear()
    januaryCalendar.set(Calendar.YEAR, periodYear)
    januaryCalendar.set(Calendar.MONTH, Calendar.JANUARY)
    januaryCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val january1st = januaryCalendar.getTime

    val aprilCalendar = Calendar.getInstance()
    aprilCalendar.clear()
    aprilCalendar.set(Calendar.YEAR, periodYear)
    aprilCalendar.set(Calendar.MONTH, Calendar.APRIL)
    aprilCalendar.set(Calendar.DAY_OF_MONTH, 5)
    val april5th = aprilCalendar.getTime


    if ((periodStart.compareTo(january1st) == 0 || periodStart.after(january1st)) && (periodStart.before(april5th) || periodStart.compareTo(april5th) == 0)) {
      (periodYear-2, periodYear-1)
    } else {
      (periodYear-1, periodYear)
    }
  }

}

object HelperManager extends HelperManager
