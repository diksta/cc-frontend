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
import _root_.models.pages.EmailRegisterPageModel
import connectors.EmailConnector
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, FormManager, HelperManager}
import form.EmailRegistrationForm
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
 * Created by ben on 25/05/16.
 */
class EmailRegistrationControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new EmailRegistrationController with CCSession with HelperManager with KeystoreService with FormManager with ChildrenManager {
    override val auditEvent  = mock[AuditEvents]
    override val cacheClient = mock[ChildcareKeystoreService]
    override val emailConnector = mock[EmailConnector]
  }


  "EmailRegistrationController" should {

    "use the correct audit event" in {
      EmailRegistrationController.auditEvent shouldBe AuditEvents
    }

    "use the email connector " in {
      EmailRegistrationController.emailConnector shouldBe EmailConnector

    }

    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/emailRegistration/keep-me-updated"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "load keep me update template" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

    }

    "GET Free Entitlement" should {

      "not respond with NOT_FOUND - freeEntitlement" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/emailRegistration/freeEntitlement"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "load keep me update template - freeEntitlement" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onPageLoadFreeEntitlement()(request))
        status(result) shouldBe Status.OK
      }

    }

    "POST" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/children/number"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down" in {

        val emailDetails = EmailRegisterPageModel(
        emailAddress = "test@test.com",
        childrenDobSelection = Some(false)
        )
        val form = EmailRegistrationForm.form.fill(emailDetails)
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to howManyChildren template when there is no value in keystore" in {
        val emailDetails = EmailRegisterPageModel(
          emailAddress = "test@test.com",
          childrenDobSelection = Some(false)
        )
        val form = EmailRegistrationForm.form.fill(emailDetails)
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/number"
      }

      "load email unavailable template - for a valid email address and dob - email capture returns unsuccessful response" in {
        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = Some(true)
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
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
            name = "Child 3",
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


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(500))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.OK

      }

        "load technical difficulties - for a valid email address and dob - email capture is down" in {
        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = Some(false)
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(Future.failed(new RuntimeException))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "load email confirmation template - for a valid email address and dob" in {
        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = Some(true)
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
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


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(200))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.OK
      }

      "return BAD_REQUEST when POST is unsuccessful" in {

        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = None
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
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


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(200))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST

      }

    }

    "POST Free Entitlement" should {

      "not respond with NOT_FOUND - freeEntitlement" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/emailRegistration/freeEntitlement"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "load email confirmation template - freeEntitlement - for a valid email address and dob" in {
        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = Some(true)
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val child1Dob = LocalDate.parse("1994-06-22T00:00:00", formatter)
        val child2Dob = LocalDate.parse("2000-04-14T00:00:00", formatter)
        val child3Dob = LocalDate.parse("2005-10-24T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(child1Dob),
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
            name = "Child 3",
            dob = Some(child2Dob),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 1",
            dob = Some(child3Dob),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(200))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitFreeEntitlement(request))
        status(result) shouldBe Status.OK
      }

      "load email confirmation template - freeEntitlement - for a valid email address and dob when child dob should not be saved" in {
        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = Some(false)
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val child1Dob = LocalDate.parse("1994-06-22T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(child1Dob),
            childCareCost = Some(BigDecimal(0.00)),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(200))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitFreeEntitlement(request))
        status(result) shouldBe Status.OK
      }


      "return BAD_REQUEST when POST is unsuccessful- freeEntitlement" in {

        val emailDetails = _root_.models.pages.EmailRegisterPageModel(
          emailAddress = "test.test@hotmail.com",
          childrenDobSelection = None
        )

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
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
            name = "Child 3",
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


        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.emailConnector.submit(any())(any())).thenReturn(HttpResponse(200))
        val form = EmailRegistrationForm.form.fill(emailDetails)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitFreeEntitlement(request))
        status(result) shouldBe Status.BAD_REQUEST

      }

    }
  }


}
