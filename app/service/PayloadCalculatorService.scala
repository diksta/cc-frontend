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

package service

import models.payload.calculator.input.{CalculatorInput, Eligibility, CalculatorPayload}
import models.payload.eligibility.output.EligibilityOutput
import play.api.Logger

/**
* Created by user on 24/03/16.
*/

object PayloadCalculatorService extends PayloadCalculatorService

trait PayloadCalculatorService extends PayloadTFCCalculatorService with PayloadTCCalculatorService with PayloadESCCalculatorService {

  def getTCCalculatorPayload(eligibilityOutput: EligibilityOutput, claimants : List[_root_.models.claimant.Claimant]): CalculatorInput  = {
    Logger.debug(s"PayloadCalculatorService.getTCCalculatorPayload")
    val tcEligibility = createTCCalculatorPayload(eligibilityOutput, claimants)
    val eligibility = Eligibility(tc = Some(tcEligibility), tfc = None, esc = None)
    CalculatorInput(payload = CalculatorPayload(eligibility = eligibility))
  }

  def getTFCCalculatorPayload(eligibilityOutput: EligibilityOutput, childrenInput : List[_root_.models.child.Child]): CalculatorInput  = {
    Logger.debug(s"PayloadCalculatorService.getTFCCalculatorPayload")
    val tfcEligibility = createTFCCalculatorPayload(eligibilityOutput, childrenInput)
    val eligibility = Eligibility(tfc = Some(tfcEligibility), tc = None, esc = None)
    CalculatorInput(payload = CalculatorPayload(eligibility = eligibility))
  }

  def getESCCalculatorPayload(eligibilityOutput: EligibilityOutput, childrenInput : List[_root_.models.child.Child], claimants : List[_root_.models.claimant.Claimant]): CalculatorInput  = {
    Logger.debug(s"PayloadCalculatorService.getESCCalculatorPayload")
    val escEligibility = createESCCalculatorPayload(eligibilityOutput, childrenInput, claimants)
    val eligibility = Eligibility(esc = Some(escEligibility), tc = None, tfc = None)
    CalculatorInput(payload = CalculatorPayload(eligibility = eligibility))
  }

}
