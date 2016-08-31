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

import config.WSHttp
import connectors.EmailConnector._
import controllers.FakeCCApplication
import controllers.keystore.CCSession
import models.email.EmailCapture
import models.payload.eligibility.input.tfc.TFCPayload
import models.payload.eligibility.output.EligibilityOutput
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Await, Future}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
/**
 * Created by user on 28/05/16.
 */
class EmailConnectorSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val mockHttp = mock[WSHttp]

  val mockConnector = new EmailConnector with CCSession {
    override def httpPost  = mockHttp
  }

  implicit val hc = HeaderCarrier()

  "Email Connector" should {

    "get email capture Result" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)
      val emailCapture = _root_.models.email.EmailCapture(
       emailAddress = "test@test.com",
       dob = Some(List(dateOfBirth)),
       england = false
      )
      when(mockConnector.httpPost.POST[EmailCapture, HttpResponse](anyString(), any(),any())(any(),any(), any())).thenReturn(HttpResponse(200))
      val result = Await.result(mockConnector.submit(emailCapture), 10 seconds)

      result.status shouldBe 200

    }

    "get email capture Result -NotFoundException" in {

      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)
      val emailCapture = _root_.models.email.EmailCapture(
        emailAddress = "test@test.com",
        dob = Some(List(dateOfBirth)),
        england = true
      )
      when(mockConnector.httpPost.POST[EmailCapture, HttpResponse](anyString(), any(),any())(any(),any(), any())).thenReturn(HttpResponse(404))
      val result = Await.result(mockConnector.submit(emailCapture), 10 seconds)

      result.status shouldBe 404

    }

    }
}
