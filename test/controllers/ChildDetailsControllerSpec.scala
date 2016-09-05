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

import java.util.Calendar

import _root_.models.child.Disability
import _root_.models.pages.{ChildDetailsDisabilityPageModel, ChildDetailsPageModel}
import controllers.helpers.HelperManager
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, FormManager}
import form.ChildDetailsForm
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
 * Created by user on 08/02/16.
 */
class ChildDetailsControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ChildDetailsController with CCSession with KeystoreService with ChildrenManager with FormManager with HelperManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ChildDetailsController" when {
    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator-qa/children/details/1"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad(index = 1)(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when child list is empty in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Some(List())))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to the how many children page when there is no value in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/number"
      }

      "load template when there is child object is present in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

        val children = Some(List(_root_.models.child.Child(
          id = 1,
          name = "Child 1",
          dob = Some(LocalDate.now),
          childCareCost = Some(BigDecimal(0.00)),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoad(1)(request))
        status(result) shouldBe OK
      }

      "load template when there is child object is present in keystore - child list has 2 children" in {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("1994-04-14T00:00:00", formatter)

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

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
        val result = await(mockController.onPageLoad(2)(request))
        status(result) shouldBe OK
      }
    }

    "load template when there is child object is present in keystore - child list has 2 children - child 1 age is between 15 and 16" in {
      val currentCalendar = Calendar.getInstance()
      currentCalendar.clear()
      currentCalendar.setTime(LocalDate.now().minusYears(15).toDate)
      val periodYear = currentCalendar.get(Calendar.YEAR)

      val octoberCalendar = Calendar.getInstance()
      octoberCalendar.clear()
      octoberCalendar.set(Calendar.YEAR, periodYear)
      octoberCalendar.set(Calendar.MONTH, Calendar.OCTOBER)
      octoberCalendar.set(Calendar.DAY_OF_MONTH, 20)

      val dateString = octoberCalendar.get(Calendar.YEAR).toString+"-04-14"
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val dateOfBirth = LocalDate.parse(dateString, formatter)

      val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

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
            nonDisabled = true
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
            nonDisabled = true
          )
        )
      ))
      when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
      val result = await(mockController.onPageLoad(2)(request))
      status(result) shouldBe OK
    }

    "post" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator-qa/children/details/1"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to cost and education template where child index is 1" in {
        val children = Some(List(
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
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = true,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())

        val result = await(mockController.onSubmit(1)(request))

        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/cost/1"

      }

      "redirect to cost and education template where child index is 3" in {


        val children = Some(List(
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
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
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
            id = 4,
            name = "Child 4",
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

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2011-09-25T00:00:00", formatter)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = false,
          nonDisabled = true)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)


        val form = ChildDetailsForm.form.fill(detailsPageModel)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())

        val result = await(mockController.onSubmit(3)(request))

        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/cost/3"
      }



      "redirect to how many children template when no value in keystore" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = true,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())

        val result = await(mockController.onSubmit(1)(request))

        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/number"
      }

      "redirect to how many children template when negative value is passed as an argument in onSubmit" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = true,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())

        val result = await(mockController.onSubmit(-1)(request))

        status(result) shouldBe SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/number"
      }

      "redirect to technical difficulties when keystore is down " in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = true,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())

        val result = await(mockController.onSubmit(1)(request))

        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "return BAD_REQUEST when POST is unsuccessful" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

        val children = Some(List(
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
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))


        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = false,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(2)(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful - loadchildren returns None" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(None)

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = false,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful - loadchildren returns Runtime exception" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val disability = ChildDetailsDisabilityPageModel(
          disabled = false,
          severelyDisabled =false,
          certifiedBlind = false,
          nonDisabled = false)
        val detailsPageModel = ChildDetailsPageModel(dob = Some(dateOfBirth), disability = disability)

        val form = ChildDetailsForm.form.fill(detailsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(1)(request))
        status(result) shouldBe Status.SEE_OTHER
      }

    }
  }
}
