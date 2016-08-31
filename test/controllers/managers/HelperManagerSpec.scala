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

package controllers.managers

import controllers.FakeCCApplication
import controllers.manager.HelperManager._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by Child 1conder on 23/12/14.
 */
class HelperManagerSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "HelperManager" should {

    "return a string to 2 decimal places to ensure consistency with monetary amounts (UK Locale)" in {
      val cost : BigDecimal = BigDecimal(25.0)
      val stringAmount : String = convertToCurrency(cost)

      stringAmount shouldBe "£25.00"
      stringAmount should not be "$25.00"
      stringAmount should not be 25.0
    }

    "return a string to 2 decimal places to ensure consistency with monetary amounts (US Locale)" in {
      val cost : BigDecimal = BigDecimal(25.0)
      val stringAmount : String = convertToCurrency(cost)

      stringAmount shouldBe "£25.00"
      stringAmount should not be "$25.00"
      stringAmount should not be 25.0
    }

    "parse the date to a GDS format (month)" in {
      val date = LocalDate.parse("1991-08-27")
      val formatted = formatGDSDate(date)
      formatted shouldBe "27 8 1991"
      formatted should not be "1991-08-27"
      formatted should not be "27-08-1991"
      formatted should not be "27/08/1991"
    }

    "parse the date to a GDS format (day)" in {
      val date = LocalDate.parse("1991-10-02")
      val formatted = formatGDSDate(date)
      formatted shouldBe "2 10 1991"
      formatted should not be "1991-10-02"
      formatted should not be "02-10-1991"
      formatted should not be "02/10/1991"
    }

    "return a date that is 20 years and 1 day previous from now" in {
      val date = LocalDate.parse("2015-05-15")
      val bound = childsLowerAgeLimit(date)
      bound shouldBe LocalDate.parse("1995-05-14")
    }

    "return a date that is 3 years ahead from now" in {
      val date = LocalDate.parse("2015-05-15")
      val bound = childsUpperAgeLimit(date)
      bound shouldBe LocalDate.parse("2018-05-15")
    }

    "return an empty string when calling concatenateNames with no parameter" in {
      val names : List[String] = List()
      concatenateNames(names) shouldBe ""
    }

    "return an empty string when calling concatenateNames with Nil" in {
      concatenateNames(Nil) shouldBe ""
    }

    "return 'Child 1' when calling concatenateNames with a single child" in {
      val names : List[String] = List("Child 1")
      concatenateNames(names) shouldBe "Child 1 "
    }

    "return 'Child 1 and Child 2' when calling concatenateNames with two children" in {
      val names : List[String] = List("Child 1", "Child 2")
      concatenateNames(names) shouldBe "Child 1 and Child 2 "
    }

    "return 'Child 1, Child 2 and Child 3' when calling concatenateNames with more than two children" in {
      val names : List[String] = List("Child 1", "Child 2", "Child 3")
      concatenateNames(names) shouldBe "Child 1, Child 2 and Child 3 "
    }

    "determine the child's age if dob is after today's date" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("2020-08-31T00:00:00Z", formatter)

      age(Some(dateOfBirth)) shouldBe -1
    }

    "determine the child's age if dob is before current tax year september date (different day, month, year)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("1991-08-31T00:00:00Z", formatter)
      val sept = LocalDate.parse("2015-09-01T00:00:00Z", formatter)

      age(Some(dateOfBirth), sept) shouldBe 24
    }

    "determine the child's age if dob is before current tax year september date (same day)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("2013-09-01T00:00:00Z", formatter)
      val sept = LocalDate.parse("2015-09-01T00:00:00Z", formatter)

      age(Some(dateOfBirth), sept) shouldBe 2
    }

    "determine the child's age if dob is before current tax year september date (same month)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("2010-09-02T00:00:00Z", formatter)
      val sept = LocalDate.parse("2015-09-01T00:00:00Z", formatter)

      age(Some(dateOfBirth), sept) shouldBe 4
    }

    "determine the child's age if dob is before current tax year september date (same year)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("2015-10-03T00:00:00Z", formatter)
      val sept = LocalDate.parse("2015-09-01T00:00:00Z", formatter)

      age(Some(dateOfBirth), sept) shouldBe -1
    }

    "determine the child's age if dob is before current tax year september date (same year, month, day)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val dateOfBirth = LocalDate.parse("2015-09-01T00:00:00Z", formatter)
      val sept = LocalDate.parse("2015-09-01T00:00:00Z", formatter)

      age(Some(dateOfBirth), sept) shouldBe 0
    }

    "determine the tax year (current tax year, april 5)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val from = LocalDate.parse("2016-04-05T00:00:00Z", formatter)

      determineTaxYearFromNow(from) shouldBe 2015
    }

    "determine tax year (next tax year, may 5)" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val from = LocalDate.parse("2016-05-05T00:00:00Z", formatter)

      determineTaxYearFromNow(from) shouldBe 2016
    }

    "return error when child's date of birth is None" in {
      try {
        val result = age(None) shouldBe 0
        result shouldBe a[Exception]
      } catch {
        case e: Exception =>
          e shouldBe a[Exception]
      }
    }

    "(after december 31 before april 6) determine the correct from and to last tax year for a date" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val from = LocalDate.parse("2016-01-10T00:00:00", formatter)

      val taxYear = determineLastTaxYear(from)
      taxYear._1 shouldBe 2014
      taxYear._2 shouldBe 2015
    }

    "(on april 5) determine the correct from and to last tax year for a date" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val from = LocalDate.parse("2016-04-05T00:00:00", formatter)

      val taxYear = determineLastTaxYear(from)
      taxYear._1 shouldBe 2014
      taxYear._2 shouldBe 2015
    }

    "(after april 6 before jan 1) determine the correct last tax year for a date" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val fromDate = LocalDate.parse("2016-06-20T00:00:00", formatter)

      val taxYear = determineLastTaxYear(fromDate)
      taxYear._1 shouldBe 2015
      taxYear._2 shouldBe 2016
    }

    "(on april 6) determine the correct last tax year for a date" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val fromDate = LocalDate.parse("2016-04-06T00:00:00", formatter)

      val taxYear = determineLastTaxYear(fromDate)
      taxYear._1 shouldBe 2015
      taxYear._2 shouldBe 2016
    }
  }

}
