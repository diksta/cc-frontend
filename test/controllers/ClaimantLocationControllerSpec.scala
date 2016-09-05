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
import controllers.manager.{FormManager, HelperManager, ClaimantManager}
import form.ClaimantLocationForm
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import org.mockito.Matchers.{eq => mockEq, _}


class ClaimantLocationControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ClaimantLocationController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ClaimantLocationController" when {

    "GET" should {

      "not respond with NOT_FOUND" in {

        val result = route(FakeRequest(GET, "/childcare-calculator-qa/parent/location"))
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

      "redirect to claimant benefit template when claimant list is None in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/parent/benefits"
      }

      "load template when there is claimant object is present and hours is None in keystore" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          ),
          hours = None,
          escVouchersAvailable = None
        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


      "load template when there is claimant object is present and hours has some value in keystore" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          ),
          hours = Some(37.5),
          escVouchersAvailable = Some("Yes"),
          whereDoYouLive = Some("England")
        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


    }

    "POST" should {

      "redirect to parent benefits screen when claimant list is None" in {
        val request = FakeRequest("POST", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/parent/benefits"
      }

      "redirect to technical difficulties when claimant list is None and onSubmit throws exception" in {
        val request = FakeRequest("POST", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any()))thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onSubmit()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator-qa/parent/location"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to do you have a partner template where location is present" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          ),
          hours = Some(22)
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            )
            ),
            hours = Some(44)
          )))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          ),
          hours = Some(22),
          whereDoYouLive = Some("Scotland")
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            )
            ),
            hours = Some(44)
          )))



        val form = ClaimantLocationForm.form.fill(Some("Scotland"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator-qa/parent/liveWithPartner"

      }


      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ClaimantLocationForm.form.fill(Some("England"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when no value in keystore" in {
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val form = ClaimantLocationForm.form.fill(Some("England"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER

      }

      "redirect to technical difficulties when saveClaimant throws and exception" in {
        val hours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(37.5)
        )

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          ),
          hours = Some(22)
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            )
            ),
            hours = Some(44)
          )))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )),
          hours = Some(37.5)
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            )),
            hours = Some(44)
          )))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = ClaimantLocationForm.form.fill(Some("England"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful" in {
        val form = ClaimantLocationForm.form.fill(Some(""))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }
}
