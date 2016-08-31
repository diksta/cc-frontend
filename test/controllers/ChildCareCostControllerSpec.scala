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

import _root_.models.child.{Disability, Education}
import _root_.models.pages.ChildCarePageModel
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, FormManager, HelperManager}
import form.{ChildCareCostKeys, ChildCareCostForm}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
 * Created by user on 09/02/16.
 */
class ChildCareCostControllerSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val mockController = new ChildCareCostController with CCSession with KeystoreService with ChildrenManager with HelperManager with FormManager with ChildCareCostKeys {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val formService = mock[FormService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ChildCareCostController" when {

    "GET" should {

      "not respond with NOT_FOUND through GET method" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/children/cost/1"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keyStore is down" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad(-1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to the template when there is no value in keyStore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad(2)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to childcare cost template when there cost presetn and education is None" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val childDOB = LocalDate.parse("2015-01-28T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(childDOB),
          education = None,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          childCareCost = Some(12.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }


      "redirect to childcare cost template when there is Some(value) in keyStore for child age less than 16 disabled" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val childDOB = LocalDate.parse("2012-01-28T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(childDOB),
          education = None,
          disability = Disability(
            disabled = true,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ),
          childCareCost = Some(12.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to childcare cost template when there is Some(value) in keyStore for child age less than 16 severely disabled" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val childDOB = LocalDate.parse("2009-01-28T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(childDOB),
          education = None,
          disability = Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          ),
          childCareCost = Some(12.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to childcare cost template when there is Some(value) in keyStore for child age less than 16 certified blind" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val childDOB = LocalDate.parse("2009-01-28T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(childDOB),
          education = None,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = true,
            nonDisabled = false
          ),
          childCareCost = Some(12.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to child education template when there is Some(value) in keyStore for child age greater than 16 and less than 20 and education is none in keystore" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1998-08-31T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          education = None,
          childCareCost = Some(34.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to child education template when there is Some(value) in keyStore for child age greater than 16 and less than 20" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1998-08-31T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
          childCareCost = Some(34.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to benefits template when there is Some(value) in keyStore for child age > 20" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1994-08-08T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          education = Some(Education(inEducation = false, startDate = None)),
          childCareCost = Some(34.0))
        ))

        val modifiedChildrenList = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          education = None,
          childCareCost = None)
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.saveChildren(mockEq(modifiedChildrenList.get))(any(), any())).thenReturn(Future.successful(modifiedChildrenList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }


      "redirect to details template from first child to second child when there is Some(value) in keyStore for child age between 15 and 16" in {
        val dateOfBirth = LocalDate.parse(LocalDate.now().minusYears(16).toString("yyyy-MM-dd"))
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          childCareCost = Some(34.0)
        ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            childCareCost = Some(22.0))
        ))

        val modifiedChildrenList = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          education = None,
          childCareCost = None
        ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            childCareCost = Some(22.0))
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.saveChildren(mockEq(modifiedChildrenList.get))(any(), any())).thenReturn(Future.successful(modifiedChildrenList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/details/2"
      }

      "return 200 if their age is in future (-1 year)" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val oneYearFromToday = LocalDate.now().plusYears(1).toString() + "T00:00:00Z"
        val dateOfBirth = LocalDate.parse(oneYearFromToday, formatter)
        val childCareCost = 0
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("GET", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to child education template when there is Some(value) in keyStore for child that turned 16 just before 1st sep" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1999-08-31T00:00:00Z", formatter)
        val childCareCost = 0
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("GET", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.OK
      }

      "redirect to benefits template when there is Some(value) in keyStore for child that turned 16 just after 1st sep" in {
        val dateOfBirth = LocalDate.parse(LocalDate.now().minusYears(16).toString("yyyy-MM-dd"))
        val childCareCost = 0
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modifiedChildrenList = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = None
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("GET", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modifiedChildrenList.get))(any(), any())).thenReturn(Future.successful(modifiedChildrenList))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

      "redirect to technical difficulties when save children throw exception" in {
        val dateOfBirth = LocalDate.parse(LocalDate.now().minusYears(16).toString("yyyy-MM-dd"))
        val childCareCost = 0
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modifiedChildrenList = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = None
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("GET", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modifiedChildrenList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }
    }

    "POST" should {

      "not respond with NOT_FOUND through POST method" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/children/cost/1"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to how many children template when no session value in keyStore" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1993-08-31T00:00:00Z", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(dateOfBirth),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ),
          childCareCost = Some(32.99))
        ))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val childCareModel = ChildCarePageModel(Some(29.80), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onSubmit(2)(request))
        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/number"
      }

      "redirect to /parent/benefits when POST is successful with value already in keyStore (modifying) (no remaining children)" in {
        val childCareCost = 0.00
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1998-08-31T00:00:00Z", formatter)
        val dateOfBirth1 = LocalDate.parse("1996-08-31T00:00:00Z", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = Some(24.0)
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth1),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = None)),
            childCareCost = Some(24.0)
          )
        ))

        val modifiedList = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = Some(24.0)
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth1),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = None
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(true))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "/childcare-calculator/children/cost/2").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modifiedList.get))(any(), any())).thenReturn(Future.successful(modifiedList))
        val result = await(mockController.onSubmit(index = 2)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

      "redirect to /children/details/2 when POST is successful with value already in keyStore (modifying) (remaining children)" in {
        val childCareCost = 32.99
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1998-08-31T00:00:00Z", formatter)
        val dateOfBirth1 = LocalDate.parse("1996-08-31T00:00:00Z", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = Some(36.0)
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth1),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            childCareCost = Some(39.0)
          )
        ))

        val modifiedList = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = None
          ),
          _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth1),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            childCareCost = Some(39.0)
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(true))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modifiedList.get))(any(), any())).thenReturn(Future.successful(modifiedList))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/details/2"
      }

      "redirect to /parent/benefits when POST is successful with value already in keyStore (modifying) when child is not in education (last child)" in {
        val childCareCost = 32.97
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1998-08-31T00:00:00Z", formatter)
        val dateOfBirth1 = LocalDate.parse("1996-08-31T00:00:00Z", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = Some(childCareCost))
        ))
        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to /parent/benefits when POST is successful with value already in keyStore (modifying) when child is exact 19 years old" in {
        val childCareCost = 32.99
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1997-03-01T00:00:00Z", formatter)

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = Some(32.99))
        ))
        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST for childcare cost when POST is unsuccessful" in {
        val data = Map("childCareCost" -> " ", "childEducation" -> "false")
        val form = ChildCareCostForm.form.bind(data)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.formService.overrideFormError(mockEq(form), mockEq("error.real"), mockEq("cc.childcare.cost.error.required"))).thenReturn(form)
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST for child education when POST is unsuccessful" in {
        val data = Map("childCareCost" -> "0.00", "childEducation" -> " ")
        val form = ChildCareCostForm.form.bind(data)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.formService.overrideFormError(mockEq(form), mockEq("error.boolean"), mockEq("cc.childcare.education.error.required"))).thenReturn(form)
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "redirect to technical difficulties when keyStore is down whilst loading children" in {
        val value = 30.99
        val childCareModel = ChildCarePageModel(Some(value), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        // load the current list of children
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "submit and redirect to benefits page if their age is in future (-1 year)" in {
              val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
              val oneYearFromToday = LocalDate.now().plusYears(1).toString() + "T00:00:00Z"
              val dateOfBirth = LocalDate.parse(oneYearFromToday, formatter)
              val childCareCost = 0
              val children = Some(List(
                _root_.models.child.Child(
                  id = 1,
                  name = "Child 1",
                  dob = Some(dateOfBirth),
                  disability = Disability(
                    disabled = false,
                    severelyDisabled = false,
                    blind = false,
                    nonDisabled = true
                  ),
                  education = Some(Education(inEducation = false, startDate = None)),
                  childCareCost = Some(36.0)
                )
              ))

              val modified = Some(List(
                _root_.models.child.Child(
                  id = 1,
                  name = "Child 1",
                  dob = Some(dateOfBirth),
                  disability = Disability(
                    disabled = false,
                    severelyDisabled = false,
                    blind = false,
                    nonDisabled = true
                  ),
                  education = Some(Education(inEducation = false, startDate = None)),
                  childCareCost = None)
              ))
              val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
              val form = ChildCareCostForm.form.fill(childCareModel)
              val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
              when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
              when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
              val result = await(mockController.onSubmit(1)(request))
              status(result) shouldBe Status.SEE_OTHER
      }

      "submit and redirect to technical difficulties if save children throws runtime exception" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val oneYearFromToday = LocalDate.now().minusYears(5).toString() + "T00:00:00Z"
        val dateOfBirth = LocalDate.parse(oneYearFromToday, formatter)
        val childCareCost = 12.54
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = Some(12.54))
        ))
        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "submit and redirect to benefits page if their age is under 19 years" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("1997-03-02T00:00:00Z", formatter)
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = None)),
            childCareCost = Some(36.0)
          )
        ))

        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = true, startDate = Some(LocalDate.now()))),
            childCareCost = None)
        ))
        val childCareModel = ChildCarePageModel(Some(0.00), Some(true))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

      "submit and redirect to details page if their age is equal or greater than 20" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val ageTwenty = LocalDate.now().minusYears(20).toString() + "T00:00:00Z"
        val dateOfBirth = LocalDate.parse(ageTwenty, formatter)
        val childCareCost = 0
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None))
          ),
           _root_.models.child.Child(
            id = 2,
            name = "Child 2",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = Some(Education(inEducation = false, startDate = None)),
            childCareCost = Some(55.0)
          )
        ))

        val modifiedList = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None
          )
        ))

        val childCareModel = ChildCarePageModel(Some(childCareCost), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modifiedList.get))(any(), any())).thenReturn(Future.successful(modifiedList))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "submit and redirect to benefits page if their age is under 15 years" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateOfBirth = LocalDate.parse("2010-03-02T00:00:00Z", formatter)
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = Some(36.0)
          )
        ))

        val modified = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            ),
            education = None,
            childCareCost = Some(23.33))
        ))
        val childCareModel = ChildCarePageModel(Some(23.33), Some(false))
        val form = ChildCareCostForm.form.fill(childCareModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(modified.get))(any(), any())).thenReturn(Future.successful(modified))
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

    }

  }
}
