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

package service

import controllers.FakeCCApplication
import models.claimant.Disability
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.audit.model.{AuditEvent, DataEvent}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, ExecutionContext}
import org.mockito.Matchers.{eq => mockEq, _}

/**
 * Created by user on 25/04/16.
 */
class AuditEventsTest extends UnitSpec with FakeCCApplication with MockitoSugar {
  implicit val request = FakeRequest()
  implicit var hc = new HeaderCarrier()

  trait ObservableAuditConnector extends AuditConnector {
    var events : ListBuffer[DataEvent] = new ListBuffer[DataEvent]

    def observedEvents : ListBuffer[DataEvent] = events

    def addEvent(auditEvent : DataEvent): Unit = {
      events = events += auditEvent
    }

    override def auditingConfig: AuditingConfig = ???
    override def sendEvent(event: AuditEvent)(implicit hc: HeaderCarrier = HeaderCarrier(), ec : ExecutionContext): Future[AuditResult] = {
      addEvent(event.asInstanceOf[DataEvent])
      Future.successful(AuditResult.Success)
    }
  }

  def createObservableAuditConnector = new ObservableAuditConnector{}

  def createAuditor(observableAuditConnector : ObservableAuditConnector) = {

    val testAuditService = new AuditService {
      override lazy val auditSource = "cc-frontend"
      override def auditConnector = observableAuditConnector
    }

    new AuditEvents {
      override val auditService = testAuditService
      override val auditDataHelper = mock[AuditDataHelper]
    }
  }

  "Audit Events" should {
    "audit how many children page data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditHowManyChildrenData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("numberOfChildren")
      event.detail("numberOfChildren") should startWith("Data")

    }

    "audit child details page data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditChildDetailsData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("childDetails")
      event.detail("childDetails") should startWith("Data")

    }

    "audit child cost and education data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditCostAndEducationsData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("costAndEducation")
      event.detail("costAndEducation") should startWith("Data")

    }

    "audit claimant benefits data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantBenefitsData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantBenefits")
      event.detail("claimantBenefits") should startWith("Data")

    }

    "audit claimant last year income data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantLastYearIncomeData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantLastYearIncome")
      event.detail("claimantLastYearIncome") should startWith("Data")

    }

    "audit claimant current year income data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantCurrentYearIncomeData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantCurrentYearIncome")
      event.detail("claimantCurrentYearIncome") should startWith("Data")

    }

    "audit claimant how many hours do you work data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantHoursData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantHowManyHoursDoYouWork")
      event.detail("claimantHowManyHoursDoYouWork") should startWith("Data")

    }

    "audit claimant esc vouchers available" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantEscVouchersAvailable("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantEscVouchersAvailable")
      event.detail("claimantEscVouchersAvailable") should startWith("Data")

    }

    "audit claimant location data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditClaimantLocation("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("claimantLocation")
      event.detail("claimantLocation") should startWith("Data")

    }

    "audit claimant do you live with  partner data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditDoYouLiveWithPartnerData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("doYouLiveWithPartner")
      event.detail("doYouLiveWithPartner") should startWith("Data")

    }

    "audit household benefits data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditHouseholdBenefits("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("householdBenefits")
      event.detail("householdBenefits") should startWith("Data")

    }

    "audit email registration data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditEmailRegistrationDetails("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("emailRegistrationDetails")
      event.detail("emailRegistrationDetails") should startWith("Data")

    }

    "audit result page details" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditResultPageDetails("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("resultPageDetails")
      event.detail("resultPageDetails") should startWith("Data")

    }

    "audit result summary details" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      val auditDataMap = Map("tfcAmount" -> "1000",  "tcAmount" ->  "2000", "escAmount" ->"3000","tcAmountByUser" ->  "4000", "ucAmountByUser" ->"5000", "tfcEligibility" -> "true", "tcEligibility" -> "false", "escEligibilityParent" -> "true", "escEligibilityPartner" ->  "",  "annualChildCareCost" -> "8000", "Child1Cost" -> "200.0", "Child2Cost" -> "500.0")

      when(auditor.auditDataHelper.getResultSummaryAuditData(any(),any(), any())).thenReturn(auditDataMap)

      val auditData = ("1000", "2000", "3000", "4000", "5000", "true", "false","true", "false", "8000")

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )))

      auditor.auditResultSummary(auditData, List(_root_.models.claimant.Claimant(id =1, _root_.models.claimant.Disability())), children)

      val event =  observableAuditConnector.events.head
      event.auditType should equal("resultSummary")
      event.detail("tfcAmount") shouldBe  "1000"
      event.detail("escAmount") shouldBe  "3000"

    }

    "audit costs per age details" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      when(auditor.auditDataHelper.getChildcareCostPerAgeAuditData(any())).thenReturn(Map("Child0" -> "200.0"))

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )))

      auditor.auditCostPerAge(children)

      val event =  observableAuditConnector.events.head
      event.auditType should equal("costsPerAge")
      event.detail("Child0") shouldBe  "200.0"
    }

    "do not audit costs per age details" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      when(auditor.auditDataHelper.getChildcareCostPerAgeAuditData(any())).thenReturn(Map[String, String]())

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = false
        )))

      auditor.auditCostPerAge(children)

      val event =  observableAuditConnector.events
      event shouldBe empty

    }

    "audit claimant and children benefits" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      val auditDataMap =  Map("partnerDisabled" -> "false", "parentSeverelyDisabled" -> "false",
        "parentIncomeBenefits" -> "false", "Child2Blind" -> "false", "Child1SeverelyDisabled" -> "false",
        "parentNoBenefits" -> "false", "Child1NoBenefit" -> "true", "partnerIncomeBenefits" -> "false",
        "parentCarersAllowance" -> "false", "Child2Disabled" -> "false", "Child1Blind" -> "false",
        "partnerCarersAllowance" -> "false", "Child2SeverelyDisabled" -> "true", "partnerNoBenefits" -> "true",
        "parentDisabled" -> "true", "Child1Disabled" -> "false", "partnerSeverelyDisabled" -> "false",
        "Child2NoBenefit" -> "false")

      when(auditor.auditDataHelper.getClaimantChildrenBenefitsAuditData(any(),any())).thenReturn(auditDataMap)

      val children = List(_root_.models.child.Child(
        id = 0,
        name = "Child1",
        dob = Some(LocalDate.now()),
        childCareCost = Some(BigDecimal(200.00)),
        disability = _root_.models.child.Disability(
          disabled = false,
          severelyDisabled = false,
          blind = false,
          nonDisabled = true
        )),
        _root_.models.child.Child(
          id = 0,
          name = "Child2",
          dob = Some(LocalDate.now()),
          childCareCost = Some(BigDecimal(500.00)),
          disability = _root_.models.child.Disability(
            disabled = false,
            severelyDisabled = true,
            blind = false,
            nonDisabled = false
          )))

      val claimants = List(_root_.models.claimant.Claimant(
        id = 1,
        disability = Disability(
          disabled = true,
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
            noBenefits = true
          )
        ))

       auditor.auditClaimantChildrenBenefits(claimants,  children)

      val event =  observableAuditConnector.events.head
      event.auditType should equal("claimantChildrenBenefits")
      event.detail("parentCarersAllowance") shouldBe  "false"
      event.detail("partnerNoBenefits") shouldBe  "true"

    }


    "audit partner benefits data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditPartnerBenefitsData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("partnerBenefits")
      event.detail("partnerBenefits") should startWith("Data")

    }

    "audit partner last year income data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditPartnerLastYearIncomeData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("partnerLastYearIncome")
      event.detail("partnerLastYearIncome") should startWith("Data")

    }

    "audit partner current year income data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditPartnerCurrentYearIncomeData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("partnerCurrentYearIncome")
      event.detail("partnerCurrentYearIncome") should startWith("Data")

    }

    "audit partner how many hours do you work data" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditPartnerHoursData("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("partnerHowManyHoursDoYouWork")
      event.detail("partnerHowManyHoursDoYouWork") should startWith("Data")

    }

    "audit partner esc vouchers available" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditPartnerEscVouchersAvailable("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("partnerEscVouchersAvailable")
      event.detail("partnerEscVouchersAvailable") should startWith("Data")

    }

    "audit number of users who express interest for TFC" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditTFCDOBSelected("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("TFCDOBSelected")
      event.detail("TFCDOBSelected") should startWith("Data")

    }

    "audit number of users who express interest for Additional Free Hours" in {

      val observableAuditConnector = createObservableAuditConnector
      val auditor = createAuditor(observableAuditConnector)

      auditor.auditAFHDOBSelected("Data")

      val event =  observableAuditConnector.events.head

      event.auditType should equal("AFHDOBSelected")
      event.detail("AFHDOBSelected") should startWith("Data")

    }


  }


}
