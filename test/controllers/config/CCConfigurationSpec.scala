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

package controllers.config

import config.ApplicationConfig
import controllers.FakeCCApplication
import org.scalatest.mock.MockitoSugar
import play.api.Play
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 08/12/14.
 */

class CCConfigurationSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  import play.api.Play.current

  "CCConfigurationSpec" should {

    "verify that the configuration for upperThreshold is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.tfc.upperThreshold").getOrElse(-1)
      config shouldBe 150000
    }

    "verify that the configuration for maxTopupPerQuarter is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.tfc.maxTopupPerQuarter").getOrElse(-1)
      config shouldBe 500
    }

    "verify that the configuration for divider is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.tfc.topup.divider").getOrElse(-1)
      config shouldBe 100
    }

    "verify that the configuration for multiplier is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.tfc.topup.multiplier").getOrElse(-1)
      config shouldBe 20
    }

    "verify that the configuration for max length of children is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.number.of.children.max.length").getOrElse(-1)
      config shouldBe 3
    }

    "verify that the configuration for minimum number of children is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.minimum.number.of.children").getOrElse(-1)
      config shouldBe 0
    }

    "verify that the configuration for maximum number of children is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.maximum.number.of.children").getOrElse(-1)
      config shouldBe 20
    }

    "verify that the configuration for the maxTopUpPerQuarter for a disabled child is what is specified" in {
      val config = Play.configuration.getNumber("variables.service.tfc.maxTopupPerQuarterDisabled").getOrElse(-1)
      config shouldBe 1000
    }

    "verify that the technical difficulties link is specified" in {
      val config = Play.configuration.getString("variables.service.tfc.technical.difficulties.gov.uk.link").getOrElse(-1)
      config shouldBe "https://www.gov.uk/help-with-childcare-costs/approved-childcare"
    }

    "verify that the email register cancel button for govuk is specified" in {
      val config = Play.configuration.getString("variables.service.gov.uk.email.register.cancel").getOrElse(-1)
      config shouldBe "https://www.gov.uk/taxfreechildcare"
    }

    "verify that the Tax-Free Childcare 2015 exclusion rule is enabled" in {
      val config : Boolean = Play.configuration.getBoolean("variables.service.september.2015.exclusion").getOrElse(false)
      config shouldBe true
    }

    "verify that the configuration for TFC Cost lower limit is specified" in {
      val config : Double = Play.configuration.getDouble("variables.service.childcare.cost.lower.limit").getOrElse(-1)
      config shouldBe 0.0
    }

    "verify that the configuration for TFC Cost upper limit is specified" in {
      val config : Double = Play.configuration.getDouble("variables.service.childcare.cost.upper.limit").getOrElse(-1)
      config shouldBe 9999.99
    }

    "verify that the configuration for child's lower dob limit in years and days is specified" in {
      val years: Int = Play.configuration.getInt("variables.service.child.lower.bound.years").getOrElse(0)
      val days: Int = Play.configuration.getInt("variables.service.child.lower.bound.days").getOrElse(0)

      years shouldBe -20
      days shouldBe -1
    }

    "verify that the configuration for child's upper dob limit in years is specified" in {
      val years: Int = Play.configuration.getInt("variables.service.child.upper.bound.years").getOrElse(0)
      years shouldBe 3
    }

    "verify that the configuration for a quarter is specified" in {
      val quarter : Int = Play.configuration.getInt("variables.service.time.quarter").getOrElse(0)
      quarter shouldBe 3
    }

    "verify the that the configuration for a disabled child's max age is specified" in {
      val years: Int = Play.configuration.getInt("variables.service.disabled.child.max.age").getOrElse(0)
      years shouldBe 16
    }

    "verify the that the configuration for a non disabled child's max age is specified" in {
      val years: Int = Play.configuration.getInt("variables.service.non.disabled.child.max.age").getOrElse(0)
      years shouldBe 11
    }

    "verify the that the configuration for september 2015 disabled exclusion is specified" in {
      val option = Play.configuration.getString("variables.service.september.2015.disabled.exclusion").getOrElse("")
      option shouldBe "1999-09-01"
    }

    "verify the that the configuration for september 2015 non disabled exclusion is specified" in {
      val option = Play.configuration.getString("variables.service.september.2015.non.disabled.exclusion").getOrElse("")
      option shouldBe "2004-09-01"
    }

    "verify that the configuration for employment maximum income is defined" in {
      val option = ApplicationConfig.maximumEmploymentIncome
      option shouldBe 1000000.00
    }

    "verify that the configuration for pension maximum is defined" in {
      val option = ApplicationConfig.maximumPensionContribution
      option shouldBe 10000.00
    }

    "verify that the configuration for benefits maximum is defined" in {
      val option = ApplicationConfig.maximumBenfitsIncome
      option shouldBe 10000.00
    }

    "verify that the configuration for employment minimum income is defined" in {
      val option = ApplicationConfig.minimumEmploymentIncome
      option shouldBe 0.00
    }

  }

}
