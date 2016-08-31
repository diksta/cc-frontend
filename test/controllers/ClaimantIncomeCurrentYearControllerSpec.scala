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
import controllers.manager.{ClaimantManager, FormManager, HelperManager}
import form.{ClaimantIncomeCurrentYearFormInstance}
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
 * Created by user on 23/02/16.
 */
class ClaimantIncomeCurrentYearControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ClaimantIncomeCurrentYearController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ClaimantIncomeCurrentYearController" when {

    "GET" should {
      "not respond with NOT_FOUND (Parent)" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/parent/income/current"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "not respond with NOT_FOUND (Partner)" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/partner/income/current"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keyStore is down (Parent)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keyStore is down (Partner)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to claimant benefit template when claimant list is None in keyStore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }

      "load template when there is claimant object present and currentIncome is None in keyStore" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = None
        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template when there is claimantList present and currentIncome is None for partner in keyStore" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = None
          )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.OK
      }

      "load template when there is claimant object present and currentIncome has some values in keyStore - employment" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = Some(_root_.models.claimant.Income(
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

      "load template when there is claimant object present and currentIncome has some values in keyStore - benefits" in {
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = None,
            pension = None,
            otherIncome = None,
            benefits = Some(BigDecimal(204.00))
          )
          ))))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template when there is 2 claimant objects present and currentIncome has some values in keyStore" in {
        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = None,
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
            currentIncome = Some(_root_.models.claimant.Income(
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

      "not respond with NOT_FOUND (Parent)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/parent/income/current"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "not respond with NOT_FOUND (Partner)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/partner/income/current"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to parent weekly hours template where parent current income is present" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
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
          currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00)),
            otherIncome = Some(BigDecimal(3000.00)),
            benefits = Some(BigDecimal(420.00))
          )
          ))))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to partner weekly hours template where parent and partner current income is present" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(BigDecimal(420.00))
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
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(300.00)),
              otherIncome = Some(BigDecimal(204.00)),
              benefits = None
            )
            ))))

        val modifiedClaimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(204.00)),
              otherIncome = Some(BigDecimal(3000.00)),
              benefits = Some(BigDecimal(420.00))
            )
            ))))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when keyStore is down" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(BigDecimal(420.00))
          )
        )

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when no value in keyStore" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(BigDecimal(420.00))
          )
        )

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to parent weekly hours template where parent current income is None" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(false),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = false,
            income = None,
            pension = None
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = false,
            income = None
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = false,
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
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            )
            ))
        ))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to parent weekly hours template where parent current income - only employment selected" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = false,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = false,
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
          currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00)),
            otherIncome = None,
            benefits = None
          )
          ))))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to parent template where parent current income - other and benefits selected" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = false,
            income = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
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
          currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome = Some(_root_.models.claimant.Income(
            employmentIncome = None,
            pension = None,
            otherIncome = Some(BigDecimal(3000.00)),
            benefits = Some(BigDecimal(420.00))
          )
          ))))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedParent.get))(any(), any())).thenReturn(Future.successful(modifiedParent))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when saveClaimant throws an exception" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(false),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = false,
            income = None,
            pension = None
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = false,
            income = None
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = false,
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
          currentIncome = Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome =  Some(_root_.models.claimant.Income(
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
            currentIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            ))))
        )

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when saveClaimant throws an exception for none current income" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(false),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = false,
            income = None,
            pension = None
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = false,
            income = None
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = false,
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
          currentIncome = None
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
            currentIncome = Some(_root_.models.claimant.Income(
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
          currentIncome =  None
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
            currentIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(10000.00)),
              pension = Some(BigDecimal(3305.00)),
              otherIncome = Some(BigDecimal(6.00)),
              benefits = None
            ))))
        )

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful (Parent)" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(-10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(BigDecimal(420.00))
          )
        )

        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful (Partner)" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(-10000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = true,
            amount = Some(BigDecimal(420.00))
          )
        )

        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful - carer's allowance selected but no benefit amount entered" in {
        val currentIncome = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some(true),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = true,
            income = Some(BigDecimal(1000.00)),
            pension = Some(BigDecimal(204.00))
          ),
          other =  _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = true,
            income = Some(BigDecimal(3000.00))
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = false,
            amount = None
          )
        )

        val claimantList = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = true,
            noBenefits = true
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(249.00)),
            benefits = None
          )
          ))))


        val form = (new ClaimantIncomeCurrentYearFormInstance).form.fill(currentIncome)
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }
}
