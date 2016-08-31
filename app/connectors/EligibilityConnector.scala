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
import models.payload.eligibility.input.esc.ESCPayload
import models.payload.eligibility.input.tc.TCPayload
import models.payload.eligibility.input.tfc.TFCPayload
import models.payload.eligibility.output.EligibilityOutput
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.Future

/**
 * Created by user on 22/03/16.
 */

object EligibilityConnector extends EligibilityConnector with ServicesConfig with CCSession {
  override def httpPost = WSHttp
}

trait EligibilityConnector {

  this: CCSession =>
  import scala.concurrent.ExecutionContext.Implicits.global

  def httpPost : WSHttp

  def postUrl(key : String) : String = s"""${key}"""

  def getTFCEligibility(tfcPayload : TFCPayload)(implicit headerCarrier: HeaderCarrier): Future[EligibilityOutput] ={
    Logger.debug(s"EligibilityConnector.getTFCEligibility ")
    httpPost.POST[TFCPayload, EligibilityOutput](postUrl(ApplicationConfig.tfcEligibilityUrl), tfcPayload)
  }

  def getTCEligibility(tcPayload : TCPayload)(implicit headerCarrier: HeaderCarrier): Future[EligibilityOutput] ={
    Logger.debug(s"EligibilityConnector.getTCEligibility ")
    httpPost.POST[TCPayload, EligibilityOutput](postUrl(ApplicationConfig.tcEligibilityUrl), tcPayload)
  }

  def getESCEligibility(escPayload : ESCPayload)(implicit headerCarrier: HeaderCarrier): Future[EligibilityOutput] ={
    Logger.debug(s"EligibilityConnector.getESCEligibility ")
    httpPost.POST[ESCPayload, EligibilityOutput](postUrl(ApplicationConfig.escEligibilityUrl), escPayload)
  }
}
