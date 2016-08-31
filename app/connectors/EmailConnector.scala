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
import models.email.EmailCapture
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.Future

object EmailConnector extends EmailConnector  with ServicesConfig with CCSession  {
  override def httpPost = WSHttp
}

trait EmailConnector extends ServicesConfig {

  this: CCSession =>
  import scala.concurrent.ExecutionContext.Implicits.global

  def httpPost : WSHttp

  def submit(emailCaptureDetails: EmailCapture)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    Logger.debug(s"EmailConnector.submit ")
    httpPost.POST[EmailCapture, HttpResponse](s"${ApplicationConfig.emailCaptureUrl}", emailCaptureDetails) map {
      result =>
        result
    }recover {
      case e : NotFoundException =>
        HttpResponse.apply(404)

      case e : Exception =>
       throw e
    }
  }

}
