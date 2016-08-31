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

package validators

import controllers.FakeCCApplication
import formatter.PeriodFormatter
import mappings.Periods
import mappings.Periods.Period
import org.scalatest.mock.MockitoSugar
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.test.UnitSpec


/**
 * Created by adamconder on 28/01/2016.
 */
class PeriodValidatorSpec extends UnitSpec with FakeCCApplication with MockitoSugar{

  val form = Form[Period](
    mapping(
      "period" -> PeriodFormatter.period
    )((p : Period) => p)((p: Period) => Option(p)))

  "PeriodValidator" should {

    "accept a 'Week' as a period" in {
      form.bind(Map(
        "period" -> "Week"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.Weekly
        )
    }

    "accept a 'Fortnightly' as a period" in {
      form.bind(Map(
        "period" -> "Fortnight"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.Fortnightly
        )
    }

    "accept a 'Monthly' as a period" in {
      form.bind(Map(
        "period" -> "Month"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.Monthly
        )
    }

    "accept a '3 Monthly (Quarterly)' as a period" in {
      form.bind(Map(
        "period" -> "3 month"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.Quarterly
        )
    }

    "accept a 'Yearly' as a period" in {
      form.bind(Map(
        "period" -> "Year"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.Yearly
        )
    }

    "accept 'INVALID' as a period" in {
      form.bind(Map(
        "period" -> "INVALID"
      )).fold(
          errors => errors.errors shouldBe empty,
          success => success shouldBe Periods.INVALID
        )
    }

    "throw a ValidationError when input is not a period type" in {
      form.bind(Map(
        "period" -> "NoWeek"
      )).fold(
        errors => errors.errors.head.message shouldBe "Please enter a valid time period.",
        success => success should not be Periods.Weekly
        )
    }

    "throw a ValidationError when input is null" in {
      form.bind(Map(
        "" -> null
      )).fold(
          errors => errors.errors.head.message shouldBe "No period type provided.",
          success => success.isInstanceOf[Periods.Period] should not be true
        )
    }

    "unbind a Period => Map" in {
      val formatter = PeriodFormatter.periodsFormatter
      val result = Map(
        "period" -> "Month"
      )
      formatter.unbind("period", Periods.Monthly) shouldBe result
    }

  }

}
