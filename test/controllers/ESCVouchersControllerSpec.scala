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

import _root_.models.child.Disability
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, ClaimantManager, FormManager, HelperManager}
import form.ESCVouchersFormInstance
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
 * Created by user on 11/07/16.
 */
class ESCVouchersControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ESCVouchersController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager with ChildrenManager{
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ESCVouchersController" when {

    "GET" should {

      "not respond with NOT_FOUND for parent" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/parent/escVouchers"))
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
        val result = await(mockController.onPageLoadPartner(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/benefits"
      }


      "load template for first time(claimant)" in {
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
          hours = Some(45),
          doYouLiveWithPartner = None,
          escVouchersAvailable = None

        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template for first time(partner)" in {
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
          hours = Some(45),
          doYouLiveWithPartner = None,
          escVouchersAvailable = Some("Yes")

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
            hours = Some(45),
            doYouLiveWithPartner = None,
            escVouchersAvailable = None

          )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.OK
      }

      "load template revisit the page(claimant)" in {
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
          hours = Some(45),
          doYouLiveWithPartner = None,
          escVouchersAvailable = Some("Yes")

        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template revisit the page(partner)" in {
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
          hours = Some(45),
          doYouLiveWithPartner = None,
          escVouchersAvailable = Some("Yes")

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
            hours = Some(45),
            doYouLiveWithPartner = None,
            escVouchersAvailable = Some("No")

          )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.OK
      }

    }

    "POST" should {

      "not respond with NOT_FOUND (Parent)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/parent/escVouchers"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "not respond with NOT_FOUND (Partner)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/partner/escVouchers"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }


      "redirect to parent location template where parent hours is present and children qualify for free entitlement" in {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val firstSept2017  = LocalDate.parse("2017-06-10T00:00:00", formatter)
        val dateOfBirth = firstSept2017.minusYears(4)

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
          )
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
          ),
          hours = Some(44),
          escVouchersAvailable = None
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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
        )))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ESCVouchersFormInstance(parent = true).form.fill(Some("Yes"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/location"
      }

      "redirect to do you live with you partner" in {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val firstSept2017  = LocalDate.parse("2017-06-10T00:00:00", formatter)
        val dateOfBirth = LocalDate.parse(LocalDate.now().toString+"T00:00:00", formatter).minusYears(10)

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
          )
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
          ),
          hours = Some(44),
          escVouchersAvailable = None
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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
        )))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ESCVouchersFormInstance(parent = true).form.fill(Some("Yes"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/liveWithPartner"
      }

      "redirect to household benefits template" in {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse(LocalDate.now().toString+"T00:00:00", formatter).minusYears(10)

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
          )
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
          ),
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
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
            hours = Some(35),
            escVouchersAvailable = None
          )
        ))

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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
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
            hours = Some(35),
            escVouchersAvailable = Some("No")
          )
        ))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ESCVouchersFormInstance(parent = false).form.fill(Some("No"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/household/benefits"
      }

      "redirect to technical difficulties when keystore is down" in {

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = new ESCVouchersFormInstance(parent = true).form.fill(Some("notSure"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when no value in keystore" in {

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val form = new ESCVouchersFormInstance(parent = false).form.fill(Some("Yes"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER

      }

      "redirect to technical difficulties when saveClaimant throws and exception" in {

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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
        )
        ))


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
          hours = Some(44),
          escVouchersAvailable = Some("No")
        )
        ))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = new ESCVouchersFormInstance(parent = false).form.fill(Some("No"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when loadchildren throws and exception" in {

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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
        )
        ))


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
          hours = Some(44),
          escVouchersAvailable = Some("notSure")
        )
        ))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Future.failed(new RuntimeException)))
        val form = new ESCVouchersFormInstance(parent = false).form.fill(Some("notSure"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when loadchildren returns None" in {

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
          hours = Some(44),
          escVouchersAvailable = Some("No")
        )
        ))


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
          hours = Some(44),
          escVouchersAvailable = Some("Yes")
        )
        ))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Future.successful(None)))
        val form = new ESCVouchersFormInstance(parent = false).form.fill(Some("Yes"))
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful" in {

        val form = new ESCVouchersFormInstance(parent = true).form.fill(None)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.BAD_REQUEST
      }


    }
  }

}
