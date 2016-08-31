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
import form.ClaimantHoursFormInstance
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
 * Created by ben on 04/03/16.
 */
class ClaimantHoursControllerSpec extends UnitSpec with MockitoSugar with FakeCCApplication {

  val mockController = new ClaimantHoursController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager with ChildrenManager {
    override val cacheClient = mock[ChildcareKeystoreService]
    override val auditEvent  = mock[AuditEvents]
  }

  "ClaimantHoursController" when {

    "GET" should {

      "not respond with NOT_FOUND" in {
        val result = route(FakeRequest(GET, "/childcare-calculator/parent/hours"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
    }

      "redirect to technical difficulties when keystore is down (Parent)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to technical difficulties when keystore is down (Partner)" in {
        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.onPageLoadPartner()(request))
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
          hours = None
         )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template when there is claimant list is present and hours is None for partner in keystore" in {
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
              pension = Some(BigDecimal(300.00)),
              otherIncome = Some(BigDecimal(204.00)),
              benefits = None
            )
            ),
            hours = None
          )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val result = await(mockController.onPageLoadPartner()(request))
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
          hours = Some(37.5)
        )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(parent))
        val result = await(mockController.onPageLoadParent()(request))
        status(result) shouldBe Status.OK
      }

      "load template when claimant list is present and hours has some value in parent and partner in keystore" in {
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
              pension = Some(BigDecimal(300.00)),
              otherIncome = Some(BigDecimal(204.00)),
              benefits = None
            )
            ),
            hours = Some(37.5)
          )))

        val request = FakeRequest("GET", "").withSession(mockController.sessionProvider.generateSessionId())
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val result = await(mockController.onPageLoadPartner()(request))
        status(result) shouldBe Status.OK
      }
    }

    "POST" should {

      "not respond with NOT_FOUND (Parent)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/parent/hours"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "not respond with NOT_FOUND (Partner)" in {
        val result = route(FakeRequest(POST, "/childcare-calculator/partner/hours"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "redirect to location page when the children qualify for free entitlement but the parent has no hours" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(BigDecimal(0.00))
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val firstSept2017 = LocalDate.parse("2017-06-14T00:00:00", formatter)
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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )
          ),
          hours = Some(BigDecimal(0.0))
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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )),
          hours = Some(BigDecimal(0.0))
        )))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/location"
      }

      "redirect to do you live with a partner template where parent hours is 0 and children do not qualify for free entitlement" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(BigDecimal(0.0))
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse(LocalDate.now.toString+"T00:00:00", formatter).minusYears(10)

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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )
          ),
          hours = Some(BigDecimal(0.0))
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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )),
          hours = Some(BigDecimal(0.0))
          )))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/liveWithPartner"
      }

      "redirect to household benefits template where partner hours is 0 and children do not qualify for free entitlement" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(BigDecimal(0.0))
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse(LocalDate.now.toString+"T00:00:00", formatter).minusYears(10)

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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )
          ),
          hours = Some(BigDecimal(40.0)),
          escVouchersAvailable = None,
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(0.00)),
              pension = Some(BigDecimal(0.00)),
              otherIncome = Some(BigDecimal(0.00)),
              benefits = None
            )
            ),
            hours = Some(BigDecimal(0.00)),
            escVouchersAvailable = None,
            doYouLiveWithPartner = None
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
            employmentIncome = Some(BigDecimal(0.00)),
            pension = Some(BigDecimal(0.00)),
            otherIncome = Some(BigDecimal(0.00)),
            benefits = None
          )),
          hours = Some(BigDecimal(40.0)),
          escVouchersAvailable = None,
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
            previousIncome = Some(_root_.models.claimant.Income(
              employmentIncome = Some(BigDecimal(0.00)),
              pension = Some(BigDecimal(0.00)),
              otherIncome = Some(BigDecimal(0.00)),
              benefits = None
            )
            ),
            hours = Some(BigDecimal(0.00)),
            escVouchersAvailable = None,
            doYouLiveWithPartner = None
          )))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/household/benefits"
      }

      "redirect to parent voucher template where parent hours is present and children qualify for free entitlement" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(37.5)
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2013-04-14T00:00:00", formatter)

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
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/escVouchers"
      }

      "redirect to vouchers template where parent hours is present and children don't qualify for free entitlement" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(37.5)
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

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
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = false, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/parent/escVouchers"
      }

      "redirect to number of children template where there is no children in the keystore" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
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
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/children/number"
      }


      "redirect to technical difficulties when load children throw an exception" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
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
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe errorPath
      }

      "redirect to vouchers page where parent is present and partner is present and partner has hours" in {
        val inputHours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(37.5)
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        val dateOfBirth = LocalDate.parse("2010-04-14T00:00:00", formatter)

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
            hours = Some(37.5)
          )))

        when(mockController.cacheClient.saveClaimants(mockEq(modifiedClaimantList.get))(any(), any())).thenReturn(Future.successful(modifiedClaimantList))
        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(claimantList))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val form = new ClaimantHoursFormInstance(parent = false, None, None).form.fill(inputHours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.SEE_OTHER
        result.header.headers.get("Location").get shouldBe "/childcare-calculator/partner/escVouchers"
      }

      "redirect to technical difficulties when keystore is down" in {
        val hours = _root_.models.pages.ClaimantHoursPageModel(
        numberOfHours = Some(37.5)
        )

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(hours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to technical difficulties when no value in keystore" in {
        val hours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(37.5)
        )

        when(mockController.cacheClient.loadClaimants()(any(), any())).thenReturn(Future.successful(None))
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(hours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
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
        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(hours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*).withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.SEE_OTHER
      }

      "return BAD_REQUEST when POST is unsuccessful (Parent)" in {
        val hours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(-37.5)
        )

        val form = new ClaimantHoursFormInstance(parent = true, None, None).form.fill(hours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitParent(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return BAD_REQUEST when POST is unsuccessful (Partner)" in {
        val hours = _root_.models.pages.ClaimantHoursPageModel(
          numberOfHours = Some(-37.5)
        )

        val form = new ClaimantHoursFormInstance(parent = false, None, None).form.fill(hours)
        val request = FakeRequest("POST", "").withFormUrlEncodedBody(form.data.toSeq: _*) .withSession(mockController.sessionProvider.generateSessionId())
        val result = await(mockController.onSubmitPartner(request))
        status(result) shouldBe Status.BAD_REQUEST
      }

    }
  }
}
