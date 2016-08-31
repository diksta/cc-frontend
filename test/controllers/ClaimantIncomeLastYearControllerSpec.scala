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
import controllers.manager.{HelperManager, ClaimantManager, FormManager}
import form.{ClaimantIncomeLastYearFormInstance, ClaimantIncomeLastYearForm}
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

/**
 * Created by user on 23/02/16.
 */
class ClaimantIncomeLastYearControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ClaimantIncomeLastYearController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent = mock[AuditEvents]
  }

     "partner ClaimantIncomeLastYearController" when {

       "GET" should {
         "not respond with NOT_FOUND" in {
           val result = route(FakeRequest(GET, "/childcare-calculator/parent/income/last"))
           result.isDefined shouldBe true
           status(result.get) should not be NOT_FOUND
         }

         "redirect to technical difficulties when keystore is down" in {
           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
           val result = await(mockController.onPageLoadParent()(request))
           status(result) shouldBe Status.SEE_OTHER
           result.header.headers.get("Location").get shouldBe errorPath
         }

         "redirect to claimant benefit template when claimant list is None in keystore" in {
           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
           val result = await(mockController.onPageLoadParent()(request))
           status(result) shouldBe Status.SEE_OTHER
           result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
         }

         "load template when there is claimant object is present and previousIncome is None in keystore" in {
           val parent = Some(List(_root_.models.claimant.Claimant(
             id = 1,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome = None
           )))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
           val result = await(mockController.onPageLoadParent()(request))
           status(result) shouldBe Status.OK
         }

         "load template when there is claimant object is present and previousIncome has some values in keystore" in {
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
             ))))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
           val result = await(mockController.onPageLoadParent()(request))
           status(result) shouldBe Status.OK
         }

         "load template when there is 2 claimant objects is present and previousIncome has some values in keystore" in {
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
             )),
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
                 pension = Some(BigDecimal(370.00)),
                 otherIncome = Some(BigDecimal(467.00)),
                 benefits = None
               )
               ))
           ))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val result = await(mockController.onPageLoadParent()(request))
           status(result) shouldBe Status.OK
         }
       }

       "POST" should {

         "not respond with NOT_FOUND" in {
           val result = route(FakeRequest(POST, "/childcare-calculator/parent/income/last"))
           result.isDefined shouldBe true
           status(result.get) should not be NOT_FOUND
         }

         "redirect to income likely to change this year template where parent previous income is present" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(

             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

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
             ))))

           val modifiedParent = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(204.00)),
               otherIncome = Some(BigDecimal(3000.00)),
               benefits = Some(BigDecimal(420.00))
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when keystore is down" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when no value in keystore" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where partner previous income is none" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = None,
               pension = None
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = None
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = None
             )
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
             )),
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
               ))
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
             ),
             previousIncome = Some(_root_.models.claimant.Income(
               employmentIncome = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(300.00)),
               otherIncome = Some(BigDecimal(204.00)),
               benefits = None
             )
             )),
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
                 employmentIncome = None,
                 pension = None,
                 otherIncome = None,
                 benefits = None
               )
               ))
           ))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where parent previous income is none (Partner)" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = None,
               pension = None
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = None
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = None
             )
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
             )),
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
               ))
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
             ),
             previousIncome = Some(_root_.models.claimant.Income(
               employmentIncome = None,
               pension = None,
               otherIncome = None,
               benefits = None
             )
             )),
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
               ))
           ))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where parent previous income - only employment selected" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = Some(BigDecimal(420.00))
             )
           )

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
               otherIncome = Some(BigDecimal(249.00)),
               benefits = Some(BigDecimal(50.00))
             )
             ))))

           val modifiedParent = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(204.00)),
               otherIncome = None,
               benefits = None
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where parent previous income - other and benefits selected" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

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
               otherIncome = Some(BigDecimal(249.00)),
               benefits = Some(BigDecimal(50.00))
             )
             ))))

           val modifiedParent = Some(List(_root_.models.claimant.Claimant(
             id = 1,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome = Some(_root_.models.claimant.Income(
               employmentIncome = None,
               pension = None,
               otherIncome = Some(BigDecimal(3000.00)),
               benefits = Some(BigDecimal(420.00))
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when saveClaimant throws and exception" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = None,
               pension = None
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = None
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = None
             )
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
             )),
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
               ))
           )

           )

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
               employmentIncome = None,
               pension = None,
               otherIncome = None,
               benefits = None
             ))
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
               ))))
           )

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "return BAD_REQUEST when POST is unsuccessful" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(-10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitParent(request))
           status(result) shouldBe Status.BAD_REQUEST
         }

       }
     }

    "Partner ClaimantIncomeLastYearController" when {

       "GET" should {
         "not respond with NOT_FOUND" in {
           val result = route(FakeRequest(GET, "/childcare-calculator/partner/income/last"))
           result.isDefined shouldBe true
           status(result.get) should not be NOT_FOUND
         }

         "redirect to technical difficulties when keystore is down" in {
           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
           val result = await(mockController.onPageLoadPartner()(request))
           status(result) shouldBe Status.SEE_OTHER
           result.header.headers.get("Location").get shouldBe errorPath
         }

         "redirect to claimant benefit template when claimant list is None in keystore" in {
           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
           val result = await(mockController.onPageLoadPartner()(request))
           status(result) shouldBe Status.SEE_OTHER
           result.header.headers.get("Location").get shouldBe "/childcare-calculator/partner/benefits"
         }

         "load template when there is claimant object and previousIncome is None in keystore" in {
           val partner = Some(List(_root_.models.claimant.Claimant(
             id = 2,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome = None
           )))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(partner))
           val result = await(mockController.onPageLoadPartner()(request))
           status(result) shouldBe Status.OK
         }

         "load template when there is claimant object and previousIncome has some values in keystore" in {
           val partner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(300.00)),
               otherIncome = Some(BigDecimal(204.00)),
               benefits = None
             )
             ))))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(partner))
           val result = await(mockController.onPageLoadPartner()(request))
           status(result) shouldBe Status.OK
         }

         "load template when there is 2 claimant objects and previousIncome has some values in keystore" in {
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
             )),
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
                 pension = Some(BigDecimal(370.00)),
                 otherIncome = Some(BigDecimal(467.00)),
                 benefits = None
               )
               ))
           ))

           val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val result = await(mockController.onPageLoadPartner()(request))
           status(result) shouldBe Status.OK
         }
       }

       "POST" should {

         "not respond with NOT_FOUND" in {
           val result = route(FakeRequest(POST, "/childcare-calculator/partner/income/last"))
           result.isDefined shouldBe true
           status(result.get) should not be NOT_FOUND
         }

         "redirect to income likely to change this year template where partner previous income is present" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(

             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           val partner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(300.00)),
               otherIncome = Some(BigDecimal(204.00)),
               benefits = None
             )
             ))))

           val modifiedPartner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(204.00)),
               otherIncome = Some(BigDecimal(3000.00)),
               benefits = Some(BigDecimal(420.00))
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedPartner.get))(any(), any())).thenReturn(Future.successful(modifiedPartner))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(partner))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when keystore is down" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when no value in keystore" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where partner previous income is none" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = None,
               pension = None
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = None
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = None
             )
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
             )),
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
               ))
             )
           )

           val modifiedPartner = Some(List(_root_.models.claimant.Claimant(
             id = 1,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome = Some(_root_.models.claimant.Income(
               employmentIncome = None,
               pension = None,
               otherIncome = None,
               benefits = None
             )
             )),
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
               ))
           ))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedPartner.get))(any(), any())).thenReturn(Future.successful(modifiedPartner))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where partner previous income - only employment selected" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = Some(BigDecimal(420.00))
             )
           )

           val partner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(300.00)),
               otherIncome = Some(BigDecimal(249.00)),
               benefits = Some(BigDecimal(50.00))
             )
             ))))

           val modifiedPartner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(204.00)),
               otherIncome = None,
               benefits = None
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedPartner.get))(any(), any())).thenReturn(Future.successful(modifiedPartner))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(partner))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to income likely to change this year template where partner previous income - other and benefits selected" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = Some(BigDecimal(10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           val partner = Some(List(_root_.models.claimant.Claimant(
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
               pension = Some(BigDecimal(300.00)),
               otherIncome = Some(BigDecimal(249.00)),
               benefits = Some(BigDecimal(50.00))
             )
             ))))

           val modifiedPartner = Some(List(_root_.models.claimant.Claimant(
             id = 2,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome = Some(_root_.models.claimant.Income(
               employmentIncome = None,
               pension = None,
               otherIncome = Some(BigDecimal(3000.00)),
               benefits = Some(BigDecimal(420.00))
             )
             ))))

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedPartner.get))(any(), any())).thenReturn(Future.successful(modifiedPartner))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(partner))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }

         "redirect to technical difficulties when saveClaimant throws and exception" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment = _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(false),
               income = None,
               pension = None
             ),
             other = _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(false),
               income = None
             ),
             benefits = _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(false),
               amount = None
             )
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
             )),
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
               ))
             )
           )

           val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
             id = 1,
             disability = _root_.models.claimant.Disability(
               disabled = false,
               severelyDisabled = false,
               incomeBenefits = false,
               carersAllowance = false,
               noBenefits = true
             ),
             previousIncome =  Some(_root_.models.claimant.Income(
               employmentIncome = None,
               pension = None,
               otherIncome = None,
               benefits = None
             ))
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
               ))))
           )

           when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
           when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.SEE_OTHER
         }


         "return BAD_REQUEST when POST is unsuccessful" in {
           val previousIncome = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
             employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
               selection = Some(true),
               income = Some(BigDecimal(-10000.00)),
               pension = Some(BigDecimal(204.00))
             ),
             other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
               selection = Some(true),
               income = Some(BigDecimal(3000.00))
             ),
             benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
               selection = Some(true),
               amount = Some(BigDecimal(420.00))
             )
           )

           val form = (new ClaimantIncomeLastYearFormInstance).form.fill(previousIncome)
           val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
           val result = await(mockController.onSubmitPartner(request))
           status(result) shouldBe Status.BAD_REQUEST
         }
       }
    }

}
