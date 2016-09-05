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

import _root_.models.claimant.Disability
import controllers.keystore.CCSession
import controllers.manager.{HelperManager, ChildrenManager, ClaimantManager, FormManager}
import form.ClaimantBenefitsFormInstance
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

class ClaimantBenefitsControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {


  val mockController = new ClaimantBenefitsController with CCSession with KeystoreService with ClaimantManager with FormManager with ChildrenManager with HelperManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val formService = mock[FormService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ClaimantBenefitsController" when {
    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator-qa/parent/benefits"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down(parent)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down (partner)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "load template(claimant benefits) when there is no value for claimant in keystore" in {
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }


      "load the template(claimant benefits) when there is Some(value) for claimant in keystore (Parent)" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )))

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load the template(claimant benefits) when there is Some(value) for claimant in keystore (Partner)" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            )
          )
        ))

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.OK
      }

      "load the template(claimant benefits) when there is Some(value) for claimant in keystore - child age is between 15 and 16" in {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.clear()
        currentCalendar.setTime(LocalDate.now().minusYears(15).toDate)
        val periodYear = currentCalendar.get(Calendar.YEAR)

        val octoberCalendar = Calendar.getInstance()
        octoberCalendar.clear()
        octoberCalendar.set(Calendar.YEAR, periodYear)
        octoberCalendar.set(Calendar.MONTH, Calendar.OCTOBER)
        octoberCalendar.set(Calendar.DAY_OF_MONTH, 20)

        val dateString = octoberCalendar.get(Calendar.YEAR).toString+"-04-14T00:00:00"
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse(dateString, formatter)

        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )))

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            )
          )
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }


      "load the template(claimant benefits) when there is Some(value) for claimant in keystore - child age is above 20" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )))

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now().minusYears(21)),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            )
          )
        ))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }


      "load benefits template when parent in keystore, no children from keystore" in {
        val parent = _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = true,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List(parent))))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/children/number"
      }

      "redirect to technical difficulties  when there is no value for claimant in keystore, exception while loading children" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }


      "redirect to technical difficulties  when there is Some(value) for claimant in keystore, exception while loading children" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when there is no claimant in the keystore claimant list" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List())))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }
    }

    "POST" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator-qa/parent/benefits"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to income template when parent not in keystore" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val modifiedParent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = true,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }


      "redirect to last years income on submit (Parent)" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val parent = _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = true,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )

        val modifiedParent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        )))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List(parent))))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/parent/income/last"
      }

      "redirect to last years income on submit (Partner)" in {
        val  partnerBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = true,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            )
          )))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = true
            )
          )))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))

        val form = (new ClaimantBenefitsFormInstance).form.fill(partnerBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
      }


      "redirect to technical difficulties when saving claimants in keystore (Parent)" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val parent = _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = true,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )

        val modifiedParent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        )))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List(parent))))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }



      "redirect to technical difficulties when keystore is down while getting claimants" in {
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down when retriving children (Parent)" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = false
        )

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down when retriving children (Partner)" in {
        val  partnerBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = false
        )

        val form = (new ClaimantBenefitsFormInstance).form.fill(partnerBenefitsPageModel)
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when saving claimants in keystore" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = true
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 2,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        val parent = _root_.models.claimant.Claimant(
          id = 2,
          disability = _root_.models.claimant.Disability(
            disabled = true,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          )
        )

        val modifiedParent = Some(List(_root_.models.claimant.Claimant(
          id = 2,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          )
        )))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List(parent))))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful (Parent)" in {
        val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = false
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))

        val form = (new ClaimantBenefitsFormInstance).form.fill(parentBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful (Partner)" in {
        val  partnerBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
          disabilityBenefit = false,
          severeDisabilityBenefit = false,
          incomeBenefit = false,
          carerAllowanceBenefit = false,
          noBenefit = false
        )

        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        ))

        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))

        val form = (new ClaimantBenefitsFormInstance).form.fill(partnerBenefitsPageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

    }
  }

}
