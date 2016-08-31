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

import controllers.FakeCCApplication
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 27/01/2016.
 */
class PeriodsSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "Periods" should {

    "(Weekly) return a string representation of itself" in {
      val period = Periods.Weekly
      Periods.toString(period) shouldBe "weekly"
    }

    "(fortnightly) return a string representation of itself" in {
      val period = Periods.Fortnightly
      Periods.toString(period) shouldBe "fortnightly"
    }


    "(Monthly) return a string representation of itself" in {
      val period = Periods.Monthly
      Periods.toString(period) shouldBe "monthly"
    }

    "(3 Monthly) return a string representation of itself" in {
      val period = Periods.Quarterly
      Periods.toString(period) shouldBe "3-monthly"
    }

    "(Yearly) return a string representation of itself" in {
      val period = Periods.Yearly
      Periods.toString(period) shouldBe "yearly"
    }

    "(Invalid) return a string representation of itself" in {
      val period = Periods.INVALID
      Periods.toString(period) shouldBe "invalid period"
    }

    "(Invalid - null) return a string representation of itself" in {
      Periods.toString(null) shouldBe "invalid period"
    }
  }

  "toPeriod" should {
    "(Monthly) return an enum when passed a string" in {
      val period = "monthly"
      Periods.toPeriod(period) shouldBe Periods.Monthly
    }

    "(3-monthly) return an enum when passed a string" in {
      val period = "3-monthly"
      Periods.toPeriod(period) shouldBe Periods.Quarterly
    }

    "(INVALID) return an enum when passed a string" in {
      val period = "every 2 weeks"
      Periods.toPeriod(period) shouldBe Periods.INVALID
    }
  }

}
