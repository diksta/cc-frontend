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

package config

import play.api.Play._
import uk.gov.hmrc.play.config.{RunMode, ServicesConfig}

trait ApplicationConfig {

  val assetsPrefix: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val analyticsToken: Option[String]
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl : String
  val useMinifiedAssets: Boolean

}

object ApplicationConfig extends ApplicationConfig with ServicesConfig with RunMode {

  val contactFormServiceIdentifier = "CC"

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing key: $key"))
  private def loadInteger(key : String) = configuration.getInt(key).getOrElse(throw new Exception(s"Missing key: $key"))

  private val contactFrontendService = baseUrl("contact-frontend")
  private val contactHost = configuration.getString(s"microservice.services.contact-frontend.host").getOrElse("")

  override lazy val assetsPrefix: String = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  override lazy val analyticsToken: Option[String] = configuration.getString(s"google-analytics.token")
  override lazy val analyticsHost: String = configuration.getString(s"google-analytics.host").getOrElse("auto")
  override lazy val useMinifiedAssets = configuration.getBoolean(s"microservice.services.cc-frontend.assets.minified").getOrElse(true)

  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val numberOfChildrenMaxLength = loadInteger("variables.service.number.of.children.max.length")
  lazy val minimumNumberOfChildren = configuration.getInt("variables.service.minimum.number.of.children").fold(0)( x => x )
  lazy val maximumNumberOfChildren = configuration.getInt("variables.service.maximum.number.of.children").fold(20)( x => x )

  lazy val minimumChildCareCost = configuration.getDouble("variables.service.childcare.cost.lower.limit").fold(0.00)(x => x)
  lazy val maximumChildCareCost = configuration.getDouble("variables.service.childcare.cost.upper.limit").fold(9999.99)( x => x )
  lazy val maxChildAge15Limit = configuration.getInt("variables.service.child.cost.upper.age.limit").fold(15)(x => x)
  lazy val maxChildAge16DisabledLimit = configuration.getInt("variables.service.child.cost.upper.age.disabled.limit").fold(16)(x => x)
  lazy val maxChildAge19Limit = configuration.getInt("variables.service.child.education.19.age.limit").fold(19)(x => x)
  lazy val maxChildAge20Limit = configuration.getInt("variables.service.child.education.20.age.limit").fold(20)(x => x)

  lazy val maximumEmploymentIncome = configuration.getDouble("variables.service.income.employment.maximum").fold(1000000.00)(x => x)
  lazy val maximumPensionContribution = configuration.getDouble("variables.service.income.pension.maximum").fold(10000.00)(x => x)
  lazy val maximumBenfitsIncome = configuration.getDouble("variables.service.income.benefits.maximum").fold(100000.00)(x => x)
  lazy val minimumEmploymentIncome = configuration.getDouble("variables.service.income.minimum").fold(0.00)(x => x)
  lazy val minimumBenefitAmountWhenCarersAllowanceSelected = configuration.getDouble("variables.service.benefit.income.minimum").fold(0.01)(x => x)

  lazy val childLowerBoundDays = configuration.getInt("variables.service.child.lower.bound.days").fold(-1)(x => x)
  lazy val childLowerBoundYears = configuration.getInt("variables.service.child.lower.bound.years").fold(-20)(x => x)
  lazy val childUpperBoundYears = configuration.getInt("variables.service.child.upper.bound.years").fold(3)(x => x)

  lazy val minimumHours = configuration.getDouble("variables.service.hours.minimum").fold(0.0)(x => x)
  lazy val maximumHours = configuration.getDouble("variables.service.hours.maximum").fold(99.5)(x => x)

  lazy val minBenefitAmount = configuration.getInt("variables.benefit.amount.minimum").fold(0)(x => x)
  lazy val tcPaymentWeeksInAYear = configuration.getInt("variables.tc.payments.weeks.in.a.year").fold(13)(x => x)
  lazy val maxBenefitAmount = configuration.getDouble("variables.benefit.amount.maximum").fold(9999.99)(x => x)

  lazy val ucFrequency = configuration.getInt("variables.uc.frequency").fold(12)(x => x)

  lazy val noOfMonths = configuration.getInt("variables.service.number.of.month").fold(12)(x => x)

  lazy val emailCaptureUrl = baseUrl("cc-email-capture") + loadConfig(s"microservice.services.cc-email-capture.email.capture.url")
  lazy val tfcEligibilityUrl = baseUrl("cc-eligibility") + loadConfig(s"microservice.services.cc-eligibility.tfc.eligibility.url")
  lazy val tcEligibilityUrl = baseUrl("cc-eligibility") + loadConfig(s"microservice.services.cc-eligibility.tc.eligibility.url")
  lazy val escEligibilityUrl = baseUrl("cc-eligibility") + loadConfig(s"microservice.services.cc-eligibility.esc.eligibility.url")

  lazy val tfcCalculatorUrl = baseUrl("cc-calculator") + loadConfig(s"microservice.services.cc-calculator.tfc.calculator.url")
  lazy val tcCalculatorUrl = baseUrl("cc-calculator") + loadConfig(s"microservice.services.cc-calculator.tc.calculator.url")
  lazy val escCalculatorUrl = baseUrl("cc-calculator") + loadConfig(s"microservice.services.cc-calculator.esc.calculator.url")

  lazy val freeEntitlementAgeLowerLimit = configuration.getInt("variable.service.free.entitlement.lower.age.limit").fold(2)(x => x)
  lazy val freeEntitlementAgeUpperLimit = configuration.getInt("variable.service.free.entitlement.upper.age.limit").fold(4)(x => x)
  lazy val freeEntitlementTwoYearOld = configuration.getInt("variable.service.free.entitlement.two.year.old").fold(2)(x => x)
  lazy val freeEntitlementThreeYearOld = configuration.getInt("variable.service.free.entitlement.three.year.old").fold(3)(x => x)
  lazy val freeEntitlementFourYearOld = configuration.getInt("variable.service.free.entitlement.four.year.old").fold(4)(x => x)

  lazy val emailAddressMaxLength = configuration.getInt("variables.email.address.max.length").fold(100)(x => x)
  lazy val tfcMaxAge = configuration.getInt("variables.tfc.max.age").fold(16)(x => x)

}
