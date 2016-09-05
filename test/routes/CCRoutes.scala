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

package routes

/**
 * Created by adamconder on 18/11/14.
 */

 object CCRoutes extends CCRoutes

trait CCRoutes {
  def rootPath = "/childcare-calculator-qa"

  def errorPath = path("/error")
  def welcomePath = path("welcome")

  def childSummaryPath = path( "/children/summary")

  def comparePath = path("/your-results")

  def emailConfirmationPath = path( "/keep-me-updated/confirmation")
  def emailRegisterPath = path("/keep-me-updated/tax-free-childcare")
  def emailRegisterGovukPath = path("/keep-me-updated/govuk")

  def path(endpoint: String) = s"${rootPath}$endpoint"
}
