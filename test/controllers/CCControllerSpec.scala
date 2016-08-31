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

/**
 * Created by adamconder on 18/11/14.
 */

import controllers.keystore.{CCSession, SecuredConstants}
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.Play
import play.api.Play.current
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import views.html.cccommon.technicalDifficulties

class CCControllerSpec extends UnitSpec with FakeCCApplication with CCSession with MockitoSugar {

  implicit val request = FakeRequest()

  val initialController = controllers.routes.HowManyChildrenController.onPageLoad.url

  "CCController" should {

    "inject the initial controller" in {
      val controller = CCController
      controller.initialController shouldBe controllers.routes.HowManyChildrenController.onPageLoad()
    }


    "redirect to welcome page controller" in {
      val controller = CCController
      implicit val request = FakeRequest()
      val result : Result = await(controller.onPageLoad()(request))
      status(result) shouldBe Status.SEE_OTHER
      controller.initialController shouldBe controllers.routes.HowManyChildrenController.onPageLoad()
    }

    "redirect to start page controller when NOSESSION is found" in {
      val controller = CCController
      val form = List()
      implicit val request = FakeRequest("GET", welcomePath).withFormUrlEncodedBody(form: _*)
      val result : Result = await(controller.onPageLoad()(request.withSession(SessionKeys.sessionId -> s"${SecuredConstants.NOSESSION}")))
      status(result) shouldBe Status.SEE_OTHER
    }

    "load the technical difficulties page" in {
      implicit val request = FakeRequest()
      val controller = CCController
      val result = await(controller.loadDifficulties()(request))
      status(result) shouldBe 500
      bodyOf(result).toString.replaceAll("&#x27;", "\'") should include(Messages("cc.technical.difficulties.heading"))
    }

    "render the technical difficulties page" in {
      val result = technicalDifficulties()(request)
      val doc = Jsoup.parse(contentAsString(result))
      val config = Play.configuration.getString("variables.service.tfc.technical.difficulties.gov.uk.link").getOrElse(-1)

      doc.getElementById("page-title").text() shouldBe Messages("cc.technical.difficulties.heading")
      doc.getElementById("service-down-information").text() shouldBe Messages("cc.technical.difficulties.text")
      doc.getElementById("technical-difficulties-external").attr("href") shouldBe config
    }


  }
}
