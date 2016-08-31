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

package connectors


import config.{ApplicationConfig, WSHttp}
import controllers.keystore.CCSession
import models.payload.calculator.input.{CalculatorInput, CalculatorPayload}
import models.payload.calculator.output.CalculatorOutput
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}

import scala.concurrent.Future

/**
 * Created by user on 31/03/16.
 */
object CalculatorConnector extends CalculatorConnector with ServicesConfig with CCSession {
  override def httpPost: HttpPost = WSHttp
}

trait CalculatorConnector {

  this: CCSession =>

  def httpPost : HttpPost

  def postUrl(key : String) : String = s"""${key}"""

  def getTFCCalculatorResult(calculatorPayload : CalculatorInput)(implicit headerCarrier: HeaderCarrier): Future[CalculatorOutput] ={
    Logger.debug(s"CalculatorConnector.getTFCCalculatorResult ")
    httpPost.POST[CalculatorInput, CalculatorOutput](postUrl(ApplicationConfig.tfcCalculatorUrl), calculatorPayload)
  }

  def getTCCalculatorResult(calculatorPayload : CalculatorInput)(implicit headerCarrier: HeaderCarrier): Future[CalculatorOutput] ={
    Logger.debug(s"CalculatorConnector.getTCCalculatorResult ")
    httpPost.POST[CalculatorInput, CalculatorOutput](postUrl(ApplicationConfig.tcCalculatorUrl), calculatorPayload)
  }

  def getESCCalculatorResult(calculatorPayload : CalculatorInput)(implicit headerCarrier: HeaderCarrier): Future[CalculatorOutput] ={
    Logger.debug(s"CalculatorConnector.getESCCalculatorResult ")
    httpPost.POST[CalculatorInput, CalculatorOutput](postUrl(ApplicationConfig.escCalculatorUrl), calculatorPayload)
  }
}
