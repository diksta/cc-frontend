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

package controllers

import java.util.UUID

import connectors.CCAuthConnector
import controllers.keystore.{CCSession, CCSessionProvider, SecuredConstants}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.mvc.{Results, Session, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 25/01/16.
 */
class CCSessionSpec extends UnitSpec with FakeCCApplication with CCSession with ScalaFutures with MockitoSugar {

  val mockController = new CCController with CCSession with CCAuthConnector {
    override val sessionProvider = mock[CCSessionProvider]
    override val initialController: Call = controllers.routes.HowManyChildrenController.onPageLoad()
  }

  "CCSession" should {

    "return NOSESSION string constant" in {
      val nosession = SecuredConstants.NOSESSION
      nosession shouldBe "NOSESSION"
    }


    "redirect to the initial controller when no session is provided" in {

      val request = FakeRequest().withSession(
        SessionKeys.sessionId -> ""
      )
      val controller = mockController
      when(controller.sessionProvider.getSessionId()(request)).thenReturn(None)
      when(controller.sessionProvider.generateSession()(request)).thenReturn(new Session())

      val result = await(controller.onPageLoad()(request))
      status(result) shouldBe 303
      result.header.headers.get("Location").get shouldBe controller.initialController.url
    }

    "redirect when an correct Session is provided " in {
      val controller = mockController
      val uuid = s"session-${UUID.randomUUID}"
      val request = FakeRequest().withSession(
        SessionKeys.sessionId -> uuid
      )

      when(controller.sessionProvider.getSessionId()(request)).thenReturn(Some(uuid))
      when(controller.sessionProvider.generateSession()(request)).thenReturn(new Session())
      val result = await(controller.onPageLoad()(request))
      status(result) shouldBe 303
      result.header.headers.get("Location").get shouldBe controller.initialController.url
    }

  }

  "CCSessionProvider" should {

     "return a future result" in {
       val provider = new CCSessionProvider
       val result = Results.Redirect(controllers.routes.CCController.onPageLoad())

       whenReady(provider.futureRequest(result)) { result =>
         result shouldBe result
       }
     }

    "redirect to unauthorised" in {
      val provider = new CCSessionProvider
      val result = provider.onUnauthorized
      result.toString shouldBe Results.Redirect(controllers.routes.CCController.onPageLoad()).toString
    }

  }

}
