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

import _root_.models.child.Disability
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, FormManager}
import form.HowManyChildrenForm
import org.joda.time.LocalDate
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
* Created by adamconder on 05/02/2016.
*/
class HowManyChildrenControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new HowManyChildrenController with CCSession with KeystoreService with ChildrenManager with FormManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val childrenService = mock[ChildrenService]
    override val formService = mock[FormService]
    override val auditEvent  = mock[AuditEvents]
  }

  "HowManyChildrenController" when {

    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/children/number"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to the template when there is no value in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to the template when there is Some(value) in keystore" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        ))))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

    }

    "POST" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/children/number"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect when POST is successful with value already in keystore (adding)" in {
        val value = "2"
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))
        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))
        val form = HowManyChildrenForm.form.fill(Some(value))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        // load the current list of children
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        // pass to children service to modify the list that's been loaded
        when(mockController.childrenService.modifyListOfChildren(mockEq(value.toInt), mockEq(children.get))).thenReturn(modified.get)
        // save the modified list of children to keystore
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/dob/1"
      }

      "redirect when POST is successful with no value already in keystore" in {
        val value = "1"
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))
        val form = HowManyChildrenForm.form.fill(Some(value))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        // load the current list of children
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        // pass to children service to create the list of children
        when(mockController.childrenService.createListOfChildren(mockEq(value.toInt))).thenReturn(children.get)
        // save the modified list of children to keystore
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/dob/1"
      }

      "redirect to technical difficulties when keystore is down whilst saving children" in {
        val value = "2"
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))
        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))
        val form = HowManyChildrenForm.form.fill(Some(value))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        // load the current list of children
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        // pass to children service to modify the list that's been loaded
        when(mockController.childrenService.modifyListOfChildren(mockEq(value.toInt), mockEq(children.get))).thenReturn(modified.get)
        // save the modified list of children to keystore
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down whilst loading children" in {
        val value = "2"
        val form = HowManyChildrenForm.form.fill(Some(value))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        // load the current list of children
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "return BAD_REQUEST when POST is unsuccessful" in {
        val data = Map("numberOfChildren" -> "  ")
        val form = HowManyChildrenForm.form.bind(data)
        Logger.debug(s"form: $form")
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.formService.overrideFormError(mockEq(form), mockEq("error.number"), mockEq("cc.how.many.children.error.not.a.number"))).thenReturn(form)
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "redirect to technical difficulties when keystore is down" in {
        val value = "2"
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))
        val form = HowManyChildrenForm.form.fill(Some(value))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

    }

  }

}
