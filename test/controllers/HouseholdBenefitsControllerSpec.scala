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
import controllers.manager.{HelperManager, FormManager}
import form.HouseholdBenefitsForm
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
 * Created by ben on 06/05/16.
 */
class HouseholdBenefitsControllerSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val mockController = new HouseholdBenefitsController with CCSession with KeystoreService with FormManager with HelperManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }
  val claimantList1 = Some(List(_root_.models.claimant.Claimant(
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
    )
  )))

  val claimantList2 = Some(List(_root_.models.claimant.Claimant(
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

  val claimantList3 = Some(List(_root_.models.claimant.Claimant(
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
    hours = None
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
        pension = Some(BigDecimal(300.00)),
        otherIncome = Some(BigDecimal(204.00)),
        benefits = None
      )
      ),
      hours = Some(37),
      escVouchersAvailable = Some("Yes")
    )))

  "HouseholdBenefitsController" when {
    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/household/benefits"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to technical difficulties when keystore is down" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.failed(new RuntimeException))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "load householdBenefits template when household object is None in keystore" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList1))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "load householdBenefits template when Partner has vouchers" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList3))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "load ClaimantBenefits template when load claimants gives empty list" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List())))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "load ClaimantBenefits template when load claimants gives None" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "load householdBenefits template when household object has some TC data in keystore" in {
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1200.00)
          ))
        ))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList2))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "load householdBenefits template when household object has some UC data in keystore" in {
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            ucAmount = Some(1000.00)
          ))
        ))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList2))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


      "load householdBenefits template when household object has some NoBenefit data in keystore" in {
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = None,
            ucAmount = None
          ))
        ))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList2))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


      "load householdBenefits template when household object isDefined but benefits is None in keystore" in {
        val household = Some(_root_.models.household.Household(
          benefits = None
        ))
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList1))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

    }

    "POST" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/household/benefits"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "save tcAmount to Keystore when household object already present" in {
        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = true,
            ucBenefitSelection  = false,
            noBenefitSelection  = false,
            tcBenefitAmount = Some(1200.00),
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1300.00),
            ucAmount = None
          ))
        ))
        val modifiedHousehold = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1200.00),
            ucAmount = None
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        when(mockController.cacheClient.saveHousehold(mockEq(modifiedHousehold.get))(any(), any())).thenReturn(Future.successful(modifiedHousehold))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }

      "save ucAmount to Keystore when household object already present" in {
        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = false,
            ucBenefitSelection  = true,
            noBenefitSelection  = false,
            tcBenefitAmount = None,
            ucBenefitAmount = Some(500.00)
          )
        )

        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = None,
            ucAmount = Some(1000.00)
          ))
        ))
        val modifiedHousehold = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = None,
            ucAmount = Some(500.00)
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        when(mockController.cacheClient.saveHousehold(mockEq(modifiedHousehold.get))(any(), any())).thenReturn(Future.successful(modifiedHousehold))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }

      "save NoBenfit to Keystore when household object with UC amount already present" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = false,
            ucBenefitSelection  = false,
            noBenefitSelection  = true,
            tcBenefitAmount = None,
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = None,
            ucAmount = Some(1000.00)
          ))
        ))
        val modifiedHousehold = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = None,
            ucAmount = None
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        when(mockController.cacheClient.saveHousehold(mockEq(modifiedHousehold.get))(any(), any())).thenReturn(Future.successful(modifiedHousehold))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }

      "save tcAmount to Keystore when household object is None" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = true,
            ucBenefitSelection  = false,
            noBenefitSelection  = false,
            tcBenefitAmount = Some(1200),
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1200.00)
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveHousehold(mockEq(household.get))(any(), any())).thenReturn(Future.successful(household))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }

      "save ucAmount to Keystore when household object is None" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = false,
            ucBenefitSelection  = true,
            noBenefitSelection  = false,
            tcBenefitAmount = None,
            ucBenefitAmount = Some(700)
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            ucAmount = Some(700.00)
          ))
        ))


        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveHousehold(mockEq(household.get))(any(), any())).thenReturn(Future.successful(household))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }

      "save noBenefits to Keystore when household object is None" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = false,
            ucBenefitSelection  = false,
            noBenefitSelection  = true,
            tcBenefitAmount = None,
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits())
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveHousehold(mockEq(household.get))(any(), any())).thenReturn(Future.successful(household))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/schemes/result"
      }


      "Redirect to technical difficulties when loadHousehold throws exception" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = true,
            ucBenefitSelection  = false,
            noBenefitSelection  = false,
            tcBenefitAmount = Some(1200.00),
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1200.00),
            ucAmount = None
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.saveHousehold(mockEq(household.get))(any(), any())).thenReturn(Future.successful(household))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "Redirect to technical difficulties when saveHousehold throws exception" in {

        val pageModel = _root_.models.pages.HouseholdBenefitsPageModel(
          benefits = _root_.models.pages.BenefitsPageModel(
            tcBenefitSelection  = true,
            ucBenefitSelection  = false,
            noBenefitSelection  = false,
            tcBenefitAmount = Some(1200.00),
            ucBenefitAmount = None
          )
        )
        val household = Some(_root_.models.household.Household(
          benefits = Some(_root_.models.household.Benefits(
            tcAmount = Some(1200.00),
            ucAmount = None
          ))
        ))

        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveHousehold(mockEq(household.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = HouseholdBenefitsForm.form.fill(pageModel)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful" in {

        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "true",
          "benefits.tcBenefitAmount" -> "&&&&&&",
          "benefits.ucBenefitAmount" -> ""
        ))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful -invalid selection" in {

        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "false",
          "benefits.ucBenefitSelection" -> "false",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> ""
        ))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful- replace error.real with You must tell us how much you get in universal credit" in {

        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.ucBenefitSelection" -> "true",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> ""
        ))

        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful - replace error.real with You must tell us how much you get in tax credits" in {

        val form = HouseholdBenefitsForm.form.bind(Map(
          "benefits.tcBenefitSelection" -> "true",
          "benefits.ucBenefitSelection" -> "false",
          "benefits.noBenefitSelection" -> "false",
          "benefits.tcBenefitAmount" -> "",
          "benefits.ucBenefitAmount" -> ""
        ))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmit(request))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }
}
