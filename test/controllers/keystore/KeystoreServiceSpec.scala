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

package controllers.keystore

/**
 * Created by adamconder on 18/11/14.
 */

import connectors.CCAuthConnector
import controllers.FakeCCApplication
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class KeystoreServiceSpec extends UnitSpec with FakeCCApplication with MockitoSugar  {

  object TestController extends TestController with CCSession with KeystoreService with CCAuthConnector

  trait TestController extends FrontendController with Actions {
    this: CCSession with KeystoreService with CCAuthConnector =>

    val cacheClient : ChildcareKeystoreService

    def testKeystoreSave() = UnauthorisedAction {
      implicit request =>
        cacheClient.cacheEntryForSession[String]("test", "calDetails").map {
          res =>
            Ok("Successfully inserted")
        } recover {
          case e : Exception =>
            e.getMessage match {
              case s : String =>
                InternalServerError(s)
              case _ =>
                InternalServerError
            }

        }
    }

    def testKeystoreFetch() = UnauthorisedAction {
      implicit request =>
        cacheClient.fetchEntryForSession[String]("test").map {
          case Some(x) =>
            Ok("fetched object")
          case None =>
            Ok("cound not fetch object")
        } recover {
          case e : Exception =>
            e.getMessage match {
              case s : String =>
                InternalServerError(s)
              case _ =>
                InternalServerError
            }
        }
    }

  }

  def testController = new TestController with KeystoreService with CCSession with CCAuthConnector {
    override val cacheClient = mock[ChildcareKeystoreService]
  }

  "KeystoreService" when {

    "GET data" should {

      "(GET) return 200 when data is not found for key" in {
        implicit val request = FakeRequest()
        val controller = testController
        /// could return Some("") or None both but return Status.OK / None is where the object hasn't been saved to keystore yet
        when(controller.cacheClient.fetchEntryForSession[String](mockEq("test"))(any(),any(),any())).thenReturn(Future.successful(Some("test")))
        val result = await(controller.testKeystoreFetch() (request))
        status(result) shouldBe Status.OK
      }

      "(GET) throw an Exception when keystore is down" in {
        implicit val request = FakeRequest()
        val controller = testController
        when(controller.cacheClient.fetchEntryForSession[String](mockEq("test"))(any(),any(),any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(controller.testKeystoreFetch() (request))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

    }

    "POST data" should {

      "(POST) Successfully insert the data to keystore" in {
        implicit val request = FakeRequest()
        val controller = testController
        /// could return Some("") or None both but return Status.OK / None is where the object hasn't been saved to keystore yet
        when(controller.cacheClient.cacheEntryForSession[String](any(), mockEq("calDetails"))(any(),any(),any())).thenReturn(Future.successful(Some("test")))
        val result = await(controller.testKeystoreSave() (request))
        status(result) shouldBe Status.OK
      }

      "(POST) throw an Exception when keystore is down" in {
        implicit val request = FakeRequest()
        val controller = testController
        when(controller.cacheClient.cacheEntryForSession[String](any(), mockEq("calDetails"))(any(),any(),any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(controller.testKeystoreSave() (request))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

    }
  }

}
