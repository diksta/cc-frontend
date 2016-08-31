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

import connectors.{CalculatorConnector, EligibilityConnector}
import controllers.keystore.CCSession
import models.payload.calculator.input.CalculatorInput
import models.payload.calculator.output.{Calculation, CalculatorOutput}
import models.payload.eligibility.input.esc.ESCPayload
import models.payload.eligibility.input.tc.TCPayload
import models.payload.eligibility.input.tfc.TFCPayload
import models.payload.eligibility.output.{EligibilityOutput, OutputEligibility}
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by user on 24/03/16.
 */


object ResultService extends ResultService with CCSession {
  override val eligibilityConnector = EligibilityConnector
  override val payLoadEligibilityService = PayloadEligibilityService
  override val calculatorConnector = CalculatorConnector
  override val payLoadCalculatorService = PayloadCalculatorService

}

trait ResultService {
  this: CCSession =>

  import scala.concurrent.ExecutionContext.Implicits.global


  val eligibilityConnector : EligibilityConnector
  val calculatorConnector : CalculatorConnector
  val payLoadEligibilityService : PayloadEligibilityService
  val payLoadCalculatorService : PayloadCalculatorService

  def getEligibilityResult(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child])(implicit headerCarrier: HeaderCarrier) : Future[EligibilityOutput] = {
    Logger.debug(s"ResultService.getEligibilityResult")
        val tfcPayload = payLoadEligibilityService.createTFCEligibilityPayload(claimants, children)
        val escPayload = payLoadEligibilityService.createESCEligibilityPayload(claimants, children)
        val tcPayload = payLoadEligibilityService.createTCEligibilityPayload(claimants, children)

        getEligibilityResult(tfcPayload, escPayload, tcPayload).map  {
          response =>
            val outputEligibility=  OutputEligibility (
              tfc = response._1.eligibility.tfc,
              esc = response._2.eligibility.esc,
              tc  = response._3.eligibility.tc
            )
            EligibilityOutput(outputEligibility)
        }
  }

  private def getEligibilityResult(tfcPayload : TFCPayload, escPayload :ESCPayload, tcPayload : TCPayload)(implicit headerCarrier: HeaderCarrier) ={
    Logger.debug(s"ResultService.getEligibilityResult")
    for {
      tfcEligibilityResult <- eligibilityConnector.getTFCEligibility(tfcPayload)
      escEligibilityResult <- eligibilityConnector.getESCEligibility(escPayload)
      tcEligibilityResult <- eligibilityConnector.getTCEligibility(tcPayload)
    } yield (tfcEligibilityResult, escEligibilityResult, tcEligibilityResult )
  }

  def getCalculatorResult(eligibilityOutput : EligibilityOutput, claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child])(implicit headerCarrier: HeaderCarrier) : Future[CalculatorOutput] = {
    Logger.debug(s"ResultService.getCalculatorResult")
    val tfcEligibility = payLoadCalculatorService.getTFCCalculatorPayload(eligibilityOutput, children)
    val escEligibility = payLoadCalculatorService.getESCCalculatorPayload(eligibilityOutput, children, claimants)
    val tcEligibility = payLoadCalculatorService.getTCCalculatorPayload(eligibilityOutput, claimants)


    getCalculatorResult(tfcEligibility, escEligibility, tcEligibility).map  {
      response =>
        val awardPeriod =  Calculation(
          tfc = response._1.calculation.tfc,
          esc = response._2.calculation.esc,
          tc  = response._3.calculation.tc
        )
        CalculatorOutput(awardPeriod)
    }
  }

  private def getCalculatorResult(tfcCalculatorPayload : CalculatorInput, escCalculatorPayload :CalculatorInput, tcCalculatorPayload : CalculatorInput)(implicit headerCarrier: HeaderCarrier) ={
    Logger.debug(s"ResultService.getCalculatorResult")
    for {
      tfcCalculatorResult <- calculatorConnector.getTFCCalculatorResult(tfcCalculatorPayload)
      esCalculatorResult <- calculatorConnector.getESCCalculatorResult(escCalculatorPayload)
      tcCalculatorResult <- calculatorConnector.getTCCalculatorResult(tcCalculatorPayload)
    } yield (tfcCalculatorResult, esCalculatorResult, tcCalculatorResult)
  }


}
