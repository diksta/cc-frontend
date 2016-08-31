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

package controllers.managers

import controllers.FakeCCApplication
import controllers.manager.ClaimantManager
import models.payload.eligibility.output.esc._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 18/02/2016.
 */
class ClaimantManagerSpec extends UnitSpec with FakeCCApplication with MockitoSugar with ClaimantManager {

  "ClaimantManager" when {

    "esc voucher avaibility" should {

      "return true when claimant selected escVoucher available as Yes" in {
        val claimant =  _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )),
          currentIncome = None,
          hours = Some(45),
          escVouchersAvailable = Some("Yes")
        )

        val result = claimantService.escVouchersAvailable(claimant)
        result shouldBe true
      }

      "return false when claimant selected escVoucher available as No" in {
        val claimant =  _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )),
          currentIncome = None,
          hours = Some(45),
          escVouchersAvailable = Some("No")
        )

        val result = claimantService.escVouchersAvailable(claimant)
        result shouldBe false
      }

      "return true when claimant selected escVoucher available as  notSure" in {
        val claimant =  _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )),
          currentIncome = None,
          hours = Some(45),
          escVouchersAvailable = Some("notSure")
        )

        val result = claimantService.escVouchersAvailable(claimant)
        result shouldBe true
      }

      "return false when claimant is working for 0 hours and te esc available vouchers screen does not appear" in {
        val claimant =  _root_.models.claimant.Claimant(
          id = 1,
          disability = _root_.models.claimant.Disability(
            disabled = false,
            severelyDisabled = false,
            incomeBenefits = false,
            carersAllowance = false,
            noBenefits = false
          ),
          previousIncome = Some(_root_.models.claimant.Income(
            employmentIncome = Some(BigDecimal(10000.00)),
            pension = Some(BigDecimal(300.00)),
            otherIncome = Some(BigDecimal(204.00)),
            benefits = None
          )),
          currentIncome = None,
          hours = Some(45),
          escVouchersAvailable = None
        )

        val result = claimantService.escVouchersAvailable(claimant)
        result shouldBe false
      }
    }

    "retrieving claimants" should {

      "return a claimant by id" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 5,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.getClaimantById(index = 2, claimants = input)
        result shouldBe _root_.models.claimant.Claimant(
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
          hours = None
        )
      }

      "return an exception when claimant list is empty" in {
        try {
          val result = claimantService.getClaimantById(List(), 6)
          result shouldBe a[Exception]
        } catch {
          case e : Exception =>
            e shouldBe a[Exception]
        }
      }

      "return an exception when retrieving claimant 0" in {
        val input = claimantService.createListOfClaimants(2)
        try {
          val result = claimantService.getClaimantById(input, 0)
          result shouldBe a[Exception]
        } catch {
          case e : Exception =>
            e shouldBe a[Exception]
        }
      }

      "return an exception when claimant id does not exist" in {
        val input = claimantService.createListOfClaimants(2)
        try {
          val result = claimantService.getClaimantById(input, 6)
          result shouldBe a[Exception]
        } catch {
          case e : Exception =>
            e shouldBe a[Exception]
        }
      }

    }

    "creating claimants" should {

      "create a list of claimant objects" in {
        val result = claimantService.createListOfClaimants()
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
      }

      "return an empty List() when provided 0" in {
        val result = claimantService.createListOfClaimants(requiredNumberOfClaimants = 0)
        result shouldBe List()
      }

      "create a list of 5 claimant objects when passed 5" in {
        val result = claimantService.createListOfClaimants(requiredNumberOfClaimants = 5)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 5,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        result.length shouldBe 5
      }

    }

    "modifying claimants" should {

      "add a claimant to an existing list" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.modifyListOfClaimants(requiredNumberOfClaimants = 2, claimants = input)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          )
        )
      }

      "add multiple claimants to an existing list" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.modifyListOfClaimants(requiredNumberOfClaimants = 4, claimants = input)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
      }

      "remove multiple claimants from an existing list" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.modifyListOfClaimants(requiredNumberOfClaimants = 2, claimants = input)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          )
        )
      }

    }

    "removing claimants" should {

      "(two claimants) drop a single claimant from the list" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          )
        )
        val result = claimantService.dropClaimantAtIndex(claimants = input, index = 2)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
      }

      "(multiple claimants) drop a single claimant from the list" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.dropClaimantAtIndex(claimants = input, index = 2)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
      }

      "(multiple claimants) return original claimant list when dropping index that does not exist" in {
        val input = List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
        val result = claimantService.dropClaimantAtIndex(claimants = input, index = 5)
        result shouldBe List(
          _root_.models.claimant.Claimant(
            id = 1,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
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
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 3,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          ),
          _root_.models.claimant.Claimant(
            id = 4,
            disability = _root_.models.claimant.Disability(
              disabled = false,
              severelyDisabled = false,
              incomeBenefits = false,
              carersAllowance = false,
              noBenefits = false
            ),
            previousIncome = None,
            currentIncome = None,
            hours = None
          )
        )
      }

    }

    "esc eligibility" should {

      "esc eligibility true single claimant" in  {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = true,
          failures = List()
        )


        val claimant = ESCOutputClaimant(
          qualifying = true,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )

        val escEligibility = ESCEligibilityOutput((List(TaxYear(
          from = periodStart,
          until = periodEnd,
          periods = List(
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                claimant
              ),
              children = List(
                child

              )
            )
          )))))

        val result = claimantService.getEscEligibility(escEligibility)
        result._1 shouldBe true
        result._2 shouldBe false
      }

      "esc eligibility true for parent and partner" in  {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = true,
          failures = List()
        )


        val parent = ESCOutputClaimant(
          qualifying = true,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )

        val partner = ESCOutputClaimant(
          qualifying = true,
          isPartner = true,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )


        val escEligibility = ESCEligibilityOutput((List(TaxYear(
          from = periodStart,
          until = periodEnd,
          periods = List(
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent,
                partner
              ),
              children = List(
                child

              )
            )
          )))))

        val result = claimantService.getEscEligibility(escEligibility)
        result._1 shouldBe true
        result._2 shouldBe true
      }

      "esc eligibility true for parent and partner across multiple tax years and periods" in  {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = true,
          failures = List()
        )


        val parent = ESCOutputClaimant(
          qualifying = true,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )

        val partner = ESCOutputClaimant(
          qualifying = true,
          isPartner = true,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )




        val escEligibility = ESCEligibilityOutput((List(TaxYear(
          from = periodStart,
          until = periodEnd,
          periods = List(
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent,
                partner.copy(qualifying = false)
              ),
              children = List(
                child

              )
            ),
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent.copy(qualifying = false),
                partner
              ),
              children = List(
                child

              )
            )
          )),
          TaxYear(
            from = periodStart,
            until = periodEnd,
            periods = List(
              ESCPeriod(
                from = periodStart,
                until = periodEnd,
                claimants = List(
                  parent,
                  partner.copy(qualifying = false)
                ),
                children = List(
                  child

                )
              ),
              ESCPeriod(
                from = periodStart,
                until = periodEnd,
                claimants = List(
                  parent.copy(qualifying = false),
                  partner
                ),
                children = List(
                  child

                )
              )
            ))
        )))

        val result = claimantService.getEscEligibility(escEligibility)
        result._1 shouldBe true
        result._2 shouldBe true
      }

      "esc eligibility false for parent and partner across multiple tax years and periods" in  {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = true,
          failures = List()
        )


        val parent = ESCOutputClaimant(
          qualifying = false,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )

        val partner = ESCOutputClaimant(
          qualifying = false,
          isPartner = true,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )




        val escEligibility = ESCEligibilityOutput((List(TaxYear(
          from = periodStart,
          until = periodEnd,
          periods = List(
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent,
                partner
              ),
              children = List(
                child

              )
            ),
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent,
                partner
              ),
              children = List(
                child

              )
            )
          )),
          TaxYear(
            from = periodStart,
            until = periodEnd,
            periods = List(
              ESCPeriod(
                from = periodStart,
                until = periodEnd,
                claimants = List(
                  parent,
                  partner
                ),
                children = List(
                  child

                )
              ),
              ESCPeriod(
                from = periodStart,
                until = periodEnd,
                claimants = List(
                  parent,
                  partner
                ),
                children = List(
                  child

                )
              )
            ))
        )))

        val result = claimantService.getEscEligibility(escEligibility)
        result._1 shouldBe false
        result._2 shouldBe false
      }

      "esc eligibility false for parent and partner" in  {

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = true,
          failures = List()
        )


        val parent = ESCOutputClaimant(
          qualifying = false,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )

        val partner = ESCOutputClaimant(
          qualifying = false,
          isPartner = false,
          eligibleMonthsInPeriod = 10,
          elements = ClaimantElements(
            vouchers = true
          ),
          failures = List()
        )


        val escEligibility = ESCEligibilityOutput((List(TaxYear(
          from = periodStart,
          until = periodEnd,
          periods = List(
            ESCPeriod(
              from = periodStart,
              until = periodEnd,
              claimants = List(
                parent,
                partner
              ),
              children = List(
                child

              )
            )
          )))))

        val result = claimantService.getEscEligibility(escEligibility)
        result._1 shouldBe false
        result._2 shouldBe false
      }

    }

  }

}
