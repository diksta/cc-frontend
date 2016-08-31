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

import _root_.models.claimant.{Income, Disability}
import _root_.models.household.Benefits
import _root_.models.payload.calculator.output.{Calculation, CalculatorOutput}
import _root_.models.payload.eligibility.output.{OutputEligibility, EligibilityOutput}
import controllers.keystore.CCSession
import controllers.manager.{ClaimantManager, HelperManager, ChildrenManager}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import service.{AuditEvents, ResultService}
import service.keystore.KeystoreService
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Matchers.{eq => mockEq, _}

import scala.concurrent.Future

class ResultsControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ResultsController with CCSession with KeystoreService with ChildrenManager with HelperManager  with ClaimantManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val resultService = mock[ResultService]
    override val auditEvents = mock[AuditEvents]
  }

  "ResultsController" when {

    "use the correct result service" in {
      ResultsController.resultService shouldBe ResultService
    }

    "GET" should {
      "redirect to technical difficulties when keystore is down(loading children)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down(loading claimant)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down(loading children, claimant list and household object)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to result page when there is some value in children and claimant" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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
            id = 1,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            )
          )))

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(claimantList.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(claimantList.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


      "redirect to result page when there is some value in children and claimant and uc amount is provided" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(ucAmount = Some(500)))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when childcare cost is None" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = None,
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page with zero value when TFC calculator returns a response but the household eligibility is false" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutputFromJson = getEligibilityOutput()
        val tfcEligibilityOutput =eligibilityOutputFromJson.eligibility.tfc.get.copy(householdEligibility = false)
        val eligibilityOutput = EligibilityOutput(OutputEligibility(tfc = Some(tfcEligibilityOutput), tc = Some(eligibilityOutputFromJson.eligibility.tc.get), esc = Some(eligibilityOutputFromJson.eligibility.esc.get)))

        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page with zero value when TC calculator returns a response with zero values but the basic element is false" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits())
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutputFromJson = getEligibilityOutput()
        val tcEligibilityOutput = getEligibilityOutputTC()
        val eligibilityOutput = EligibilityOutput(OutputEligibility(tc = Some(tcEligibilityOutput.eligibility.tc.get), tfc = Some(eligibilityOutputFromJson.eligibility.tfc.get), esc = Some(eligibilityOutputFromJson.eligibility.esc.get)))

        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page with zero value when TC calculator returns a response but the basic element is false" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits())
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutputFromJson = getEligibilityOutput()
        val tcEligibilityOutput = getTCEligibilityOutput()
        val eligibilityOutput = EligibilityOutput(OutputEligibility(tc = Some(tcEligibilityOutput.eligibility.tc.get), tfc = Some(eligibilityOutputFromJson.eligibility.tfc.get), esc = Some(eligibilityOutputFromJson.eligibility.esc.get)))

        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when calculator returns None as the response for tfc, esc, tc" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = CalculatorOutput(Calculation(tfc = None, tc = None, esc = None))
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to technical difficulties  when there is eligibility microservice returns exception" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.failed(new RuntimeException))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties  when there is calculator microservice returns exception" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.failed(new RuntimeException))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when there is no value in children" in {
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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when there is no value in claimant" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when there is no value in household" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when there is no value in claimant, children and household" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when claimant, children and hosuehold object is empty" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Some(List())))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(Some(List())))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(Some(_root_.models.household.Household())))
        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "Free Entitlement for England should show if child of age 2, 3 or 4 is present" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2014-04-14T00:00:00", formatter)
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(dateOfBirth),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          ))))
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          whereDoYouLive = Some("England")
        )))

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = CalculatorOutput(Calculation(tfc = None, tc = None, esc = None))
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "Free Entitlement for England should show if child will be 3 or 4 by 1st September 2017" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirthJohn = LocalDate.parse("2014-09-01T00:00:00", formatter)
        val dateOfBirthSmith = LocalDate.parse("2012-09-12T00:00:00", formatter)
        val children = Some(List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirthSmith),
            childCareCost = Some(BigDecimal(0.00)),
            disability = _root_.models.child.Disability(
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
            disability = _root_.models.child.Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            )
          )
        ))
        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = true
          ),
          whereDoYouLive = Some("England")
        )))

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = CalculatorOutput(Calculation(tfc = None, tc = None, esc = None))
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when user is already getting Tax credit" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = Some(10)))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when user doesn't have childcare cost and is eligible for tfc" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when user doesn't have childcare cost and is eligible for esc" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

        val parent = Some(List(_root_.models.claimant.Claimant(
          id = 1,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(20000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )
          )
        )))

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = None))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getESCEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }

      "redirect to result page when user is getting Tax credit but the value is very low" in {
        val children = Some(List(_root_.models.child.Child(
          id = 0,
          name = "Child 1",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(0.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          ))))

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

        val household = Some(_root_.models.household.Household(
          benefits = Some(Benefits(tcAmount = Some(500)))
        )
        )

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        when(mockController.cacheClient.loadHousehold()(any(), any())).thenReturn(Future.successful(household))
        val eligibilityOutput = getEligibilityOutput()
        when(mockController.resultService.getEligibilityResult(mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(eligibilityOutput))

        val calculatorOutput  = getCalculatorOutput()
        when(mockController.resultService.getCalculatorResult(mockEq(eligibilityOutput), mockEq(parent.get), mockEq(children.get))(any())).thenReturn(Future.successful(calculatorOutput))

        val result = await(mockController.onPageLoad()(request))
        status(result) shouldBe Status.OK
      }


    }

  }



  private def getCalculatorOutput() =  {
    val outputJson = Json.parse(
      s"""
      {
        "calculation" : {
          "tfc": {
            "from": "2016-08-27",
            "until": "2016-11-27",
            "householdContribution": {
              "parent": 6500.00,
              "government": 1000.00,
              "totalChildCareSpend": 7500.00
            },
            "numberOfPeriods" : 1,
            "periods" : [
              {
                "from": "2016-08-27",
                "until": "2016-11-27",
                "periodContribution": {
                  "parent": 6500.00,
                  "government": 1000.00,
                  "totalChildCareSpend": 7500.00
                },
                "children": [
                  {
                    "id": 0,
                    "name" : "Child 1",
                    "childCareCost": 2500.00,
                    "childContribution" : {
                      "parent": 6500.00,
                      "government": 1000.00,
                      "totalChildCareSpend": 7500.00
                    },
                    "timeToMaximizeTopUp" : 0,
                    "failures" : []
                  }
                ]
              }
            ]
          },
         "esc": {
                "from": "2016-08-27",
                "until": "2017-04-06",
                "totalSavings": {
                  "totalSaving": 0,
                  "taxSaving": 0,
                  "niSaving": 0
                },
                "taxYears": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "totalSavings": {
                      "totalSaving": 0,
                      "taxSaving": 0,
                      "niSaving": 0
                    },
                    "claimants": [
                      {
                        "qualifying": false,
                        "eligibleMonthsInTaxYear": 0,
                        "isPartner": false,
                        "escAmount": 200,
                        "escAmountPeriod": "Month",
                        "escStartDate": "2013-08-27",
                        "maximumRelief": 124,
                        "maximumReliefPeriod": "Month",
                        "income": {
                          "taxablePay": 50000,
                          "gross": 50000,
                          "taxCode": "1100L",
                          "niCategory": "A"
                        },
                        "elements": {
                          "vouchers": false
                        },
                        "savings": {
                          "totalSaving": 0,
                          "taxSaving": 0,
                          "niSaving": 0
                        },
                        "taxAndNIBeforeSacrifice": {
                          "taxPaid": 766.6,
                          "niPaid": 361.0
                        },
                        "taxAndNIAfterSacrifice": {
                          "taxPaid": 717.0,
                          "niPaid": 358.52
                        }
                      }
                    ]
                  }
                ]
              },
            "tc": {
             "from": "2016-09-27",
             "until": "2017-04-06",
             "proRataEnd" : "2017-06-20",
             "totalAwardAmount": 2989.74,
             "totalAwardProRataAmount" :0.00,
             "houseHoldAdviceAmount": 0.00,
             "totalHouseHoldAdviceProRataAmount" :0.00,
             "taxYears": [
             {
                 "from": "2016-06-20",
                 "until": "2017-04-06",
                  "proRataEnd" : "2017-06-20",
                 "taxYearAwardAmount": 2989.74,
                 "taxYearAwardProRataAmount" : 0.00,
                 "taxYearAdviceAmount": 0.00,
                 "taxYearAdviceProRataAmount" : 0.00,
                 "periods": [
                   {
                    "from": "2016-09-27",
                    "until": "2016-12-12",
                     "periodNetAmount": 2989.74,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                        "wtcWorkElement": {
                          "netAmount": 92.27,
                          "maximumAmount": 995.60,
                          "taperAmount": 903.33
                        },
                        "wtcChildcareElement": {
                          "netAmount": 704.87,
                          "maximumAmount": 704.87,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 2078.60,
                          "maximumAmount": 2078.60,
                          "taperAmount": 0.00
                        },
                        "ctcFamilyElement": {
                          "netAmount": 114.00,
                          "maximumAmount": 114.00,
                          "taperAmount": 0.00
                        }
                      }
                  },
                  {
                    "from": "2016-12-12",
                    "until": "2017-04-06",
                    "periodNetAmount": 0.00,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                      "wtcWorkElement": {
                          "netAmount": 0.00,
                          "maximumAmount":  872.85,
                          "taperAmount":  872.85
                        },
                        "wtcChildcareElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                          },
                        "ctcFamilyElement": {
                          "maximumAmount": 0.00,
                          "netAmount": 0.00,
                          "taperAmount": 0.00
                        }
                      }
                    }
                 ]
              },
               {
                 "from": "2017-04-06",
                 "until": "2018-04-06",
                 "proRataEnd" : "2017-06-20",
                 "taxYearAwardAmount": 2989.74,
                 "taxYearAwardProRataAmount" : 0.00,
                 "taxYearAdviceAmount": 0.00,
                 "taxYearAdviceProRataAmount" : 0.00,
                 "periods": [
                   {
                    "from": "2017-09-27",
                    "until": "2017-12-12",
                     "periodNetAmount": 2989.74,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                        "wtcWorkElement": {
                          "netAmount": 92.27,
                          "maximumAmount": 995.60,
                          "taperAmount": 903.33
                        },
                        "wtcChildcareElement": {
                          "netAmount": 704.87,
                          "maximumAmount": 704.87,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 2078.60,
                          "maximumAmount": 2078.60,
                          "taperAmount": 0.00
                        },
                        "ctcFamilyElement": {
                          "netAmount": 114.00,
                          "maximumAmount": 114.00,
                          "taperAmount": 0.00
                        }
                      }
                  },
                  {
                    "from": "2017-12-12",
                    "until": "2018-04-06",
                    "periodNetAmount": 0.00,
                    "periodAdviceAmount": 0.00,
                    "elements": {
                      "wtcWorkElement": {
                          "netAmount": 0.00,
                          "maximumAmount":  872.85,
                          "taperAmount":  872.85
                        },
                        "wtcChildcareElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                        },
                        "ctcIndividualElement": {
                          "netAmount": 0.00,
                          "maximumAmount": 0.00,
                          "taperAmount": 0.00
                          },
                        "ctcFamilyElement": {
                          "maximumAmount": 0.00,
                          "netAmount": 0.00,
                          "taperAmount": 0.00
                        }
                      }
                    }
                 ]
              }
             ]
           }
        }
      }
        """.stripMargin)
    val output = outputJson.validate[CalculatorOutput]
    output.get
  }

  private def getEligibilityOutput() =  {
    val outputJson = Json.parse(
      s"""
      {
        "eligibility": {
          "esc": {
            "taxYears": [
              {
                "from": "2016-08-27",
                "until": "2017-04-06",
                "periods": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "claimants": [
                      {
                        "qualifying": false,
                        "isPartner": false,
                        "eligibleMonthsInPeriod": 0,
                        "elements": {
                          "vouchers": false
                        },
                        "failures": []
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "qualifying": true,
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
          "tc": {
            "eligible": true,
            "taxYears": [
              {
                "from": "2016-06-20",
                "until": "2017-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                   "from": "2017-04-06",
                    "until": "2018-04-06",
                    "householdElements": {
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              },
               {
                "from": "2017-04-06",
                "until": "2018-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "householdElements": {
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
           "tfc": {
            "from": "2016-08-27",
            "until": "2016-11-27",

            "householdEligibility": true,
            "periods": [
              {
                "from" : "2016-08-27",
                "until" : "2016-11-27",
                "periodEligibility" : true,
                "claimants" : [
                  {
                    "qualifying" : true,
                    "isPartner" : false,
                    "failures" : []
                  }
                ],
                "children" : [
                  {
                    "id" : 0,
                    "name" : "Venky",
                    "qualifying" : true,
                    "from" : "2016-08-27",
                    "until" : "2016-11-27",
                    "failures" : []
                  }
                ]
              }
            ]
          }
        }
      }
        """.stripMargin)
    val output = outputJson.validate[EligibilityOutput]
    output.get
  }

  private def getESCEligibilityOutput() =  {
    val outputJson = Json.parse(
      s"""
      {
        "eligibility": {
          "esc": {
            "taxYears": [
              {
                "from": "2016-08-27",
                "until": "2017-04-06",
                "periods": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "eligibleMonthsInPeriod": 0,
                        "elements": {
                          "vouchers": true
                        },
                        "failures": []
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "qualifying": true,
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
          "tc": {
            "eligible": true,
            "taxYears": [
              {
                "from": "2016-06-20",
                "until": "2017-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                   "from": "2017-04-06",
                    "until": "2018-04-06",
                    "householdElements": {
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              },
               {
                "from": "2017-04-06",
                "until": "2018-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "householdElements": {
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
           "tfc": {
            "from": "2016-08-27",
            "until": "2016-11-27",

            "householdEligibility": true,
            "periods": [
              {
                "from" : "2016-08-27",
                "until" : "2016-11-27",
                "periodEligibility" : true,
                "claimants" : [
                  {
                    "qualifying" : true,
                    "isPartner" : false,
                    "failures" : []
                  }
                ],
                "children" : [
                  {
                    "id" : 0,
                    "name" : "Venky",
                    "qualifying" : true,
                    "from" : "2016-08-27",
                    "until" : "2016-11-27",
                    "failures" : []
                  }
                ]
              }
            ]
          }
        }
      }
        """.stripMargin)
    val output = outputJson.validate[EligibilityOutput]
    output.get
  }

  private def getTCEligibilityOutput() =  {

    val outputJson = Json.parse(
      s"""
      {
        "eligibility": {
          "esc": null,
          "tc": {
            "eligible": true,
            "taxYears": [
              {
                "from": "2016-08-27",
                "until": "2017-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                    "from": "2016-08-27",
                    "until": "2017-04-06",
                    "householdElements": {
                      "basic": false,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  },
                  {
                    "from": "2017-04-06",
                    "until": "2018-04-06",
                    "householdElements": {
                      "basic": true,
                      "hours30": true,
                      "childcare": true,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 3000.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": true
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
           "tfc": null
        }
      }
        """.stripMargin)

    val output = outputJson.validate[EligibilityOutput]
    output.get
  }

  private def getEligibilityOutputTC() =  {

    val outputJson = Json.parse(
      s"""
      {
        "eligibility": {
          "esc": null,
          "tc": {
            "eligible": true,
            "taxYears": [
              {
                "from": "2016-04-06",
                "until": "2017-04-06",
                "houseHoldIncome": 0.00,
                "periods": [
                  {
                    "from": "2016-04-06",
                    "until": "2017-04-06",
                    "householdElements": {
                      "basic": false,
                      "hours30": false,
                      "childcare": false,
                      "loneParent": true,
                      "secondParent": false,
                      "family": true
                    },
                    "claimants": [
                      {
                        "qualifying": true,
                        "isPartner": false,
                        "claimantDisability": {
                          "disability": false,
                          "severeDisability": false
                        },
                        "failures": [
                        ]
                      }
                    ],
                    "children": [
                      {
                        "id": 0,
                        "name": "Child 1",
                        "childcareCost": 0.00,
                        "childcareCostPeriod": "Month",
                        "qualifying": true,
                        "childElements":
                        {
                          "child": true,
                          "youngAdult": false,
                          "disability": false,
                          "severeDisability": false,
                          "childcare": false
                        },
                        "failures": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
          "tfc": null
        }
      }
        """.stripMargin)

    val output = outputJson.validate[EligibilityOutput]
    output.get
  }

}
