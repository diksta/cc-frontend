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


import controllers.keystore.CCSession
import controllers.manager.{ClaimantManager, FormManager}
import form.DoYouLiveWithPartnerForm
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.test.UnitSpec
import views.html.doYouLiveWithPartner
import org.mockito.Matchers.{eq => mockEq, _}

import scala.concurrent.Future

/**
 * Created by user on 11/04/16.
 */
class DoYouLiveWithPartnerControllerSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val mockController = new DoYouLiveWithPartnerController with CCSession with KeystoreService with FormManager with ClaimantManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "DoYouLiveWithPartnerController" when {

    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/parent/liveWithPartner"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to parent benefits screen when claimant list is is None" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }


      "load template when no selection is made where the parent is entitled to free entitlment" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          hours = None,
          escVouchersAvailable = None,
          whereDoYouLive = Some("england"),
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "load template when no selection is made" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          hours = None,
          escVouchersAvailable = None,
          whereDoYouLive = None,
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "load template when selection is made" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          hours = Some(37.5),
          escVouchersAvailable = Some("Yes"),
          whereDoYouLive = None,
          doYouLiveWithPartner = Some(true)
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

    }

    "POST" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/parent/liveWithPartner"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return BAD_REQUEST when POST is unsuccessful" in {
        val data = Map("doYouLiveWithYourPartner" -> " ")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.BAD_REQUEST

      }

      "redirect to parent benefits screen when claimant list is None" in {
        val request = FakeRequest("POST", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

      "redirect to technical difficulties when claimant list is None and onSubmit throws exception" in {
        val request = FakeRequest("POST", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any()))thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to partner benefits screen if you select yes" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(true)
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None,
            doYouLiveWithPartner = None
          )))

        when(mockController.cacheClient.saveClaimants(any())(any(), any())).thenReturn(Future.successful(modifiedClaimantList))

        val data = Map("doYouLiveWithYourPartner" -> "true")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER

      }

      "redirect to partner benefits screen if we are revisting the screen and we select yes and previously also yes was selecting" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(true)
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = true
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None,
            doYouLiveWithPartner = None
          )
        ))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(true)
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None,
            doYouLiveWithPartner = None
          )))

        when(mockController.cacheClient.saveClaimants(any())(any(), any())).thenReturn(Future.successful(modifiedClaimantList))

        val data = Map("doYouLiveWithYourPartner" -> "true")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER

      }

      "redirect to householdBenefits screen if you select no" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(false)
        )
        ))

        when(mockController.cacheClient.saveClaimants(any())(any(), any())).thenReturn(Future.successful(modifiedClaimantList))

        val data = Map("doYouLiveWithYourPartner" -> "false")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/household/benefits"
      }

      "redirect to householdBenefits screen if you select no and initially you had selected yes" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(true)
        ),
          _root_.models.claimant.Claimant(
            id = 2,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None,
            doYouLiveWithPartner = None
          )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(false)
        )))

        when(mockController.cacheClient.saveClaimants(any())(any(), any())).thenReturn(Future.successful(modifiedClaimantList))

        val data = Map("doYouLiveWithYourPartner" -> "false")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/household/benefits"

      }

      "redirect to partner benefit controller when load claimants returns None" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(None)

        val data = Map("doYouLiveWithYourPartner" -> "false")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER

      }

      "redirect to technical difficulties when load claimants throws excpetion" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          whereDoYouLive = Some("england"),
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val data = Map("doYouLiveWithYourPartner" -> "false")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath

      }

      "redirect to technical difficulties if save claimants throws runtime exception" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = None
        )))

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = None,
          currentIncome = None,
          hours = None,
          doYouLiveWithPartner = Some(false)
        )
        ))

        when(mockController.cacheClient.saveClaimants(any())(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val data = Map("doYouLiveWithYourPartner" -> "false")
        val form  = DoYouLiveWithPartnerForm.form.bind(data)

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath

      }

    }

  }

}
