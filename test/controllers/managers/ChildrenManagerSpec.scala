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

import _root_.models.child.Disability
import controllers.FakeCCApplication
import controllers.keystore.CCSession
import controllers.manager.ChildrenManager
import controllers.manager.HelperManager._
import models.payload.eligibility.output.esc._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 08/02/2016.
 */
class ChildrenManagerSpec extends UnitSpec with FakeCCApplication with CCSession with ChildrenManager {


  "ChildrenManager" when {

    "creating children" should {

      "create a list of children objects" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 1)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
      }

      "return an empty List() when provided 0" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 0)
        result shouldBe List()
      }

      "create a list of 5 child objects when passed 5" in {
        val result = childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = None,
            childCareCost = None,
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
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 4,
            name = "Child 4",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 5,
            name = "Child 5",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        result.length shouldBe 5
      }

    }

    "modifying children" should {

      "(remove) modify a list of children objects" in {
        val input = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = Some(LocalDate.now()),
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
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 1, children = input)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
      }

      "(remove) drop multiple children objects" in {
        val input = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = None,
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
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 4,
            name = "Child 4",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 5,
            name = "Child 5",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.modifyListOfChildren(requiredNumberOfChildren = 2, children = input)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = Some(LocalDate.now()),
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
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
      }


      "(add) modify existing list of children by adding 2 more children" in {
        val inputChildList = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ))

        val result = childrenService.modifyListOfChildren(3, inputChildList)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = Some(LocalDate.now()),
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
            childCareCost = None,
            dob = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
            childCareCost = None,
            dob = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
      }

      "(same) modify a list of children objects" in {
        val input = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = None,
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
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )

        val result = childrenService.modifyListOfChildren(2, input)
        result shouldBe List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(LocalDate.now()),
            childCareCost = None,
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
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
      }
    }

    "retrieving childById" should {

      "return a child by index" in {
        val children = childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        val result = childrenService.getChildById(2, children)
        result shouldBe _root_.models.child.Child(
          id = 2,
          name = "Child 2",
          dob = None,
          childCareCost = None,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          )
        )
      }

      "return last child by index" in {
        val children = childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        val result = childrenService.getChildById(5, children)
        result shouldBe _root_.models.child.Child(
          id = 5,
          name = "Child 5",
          childCareCost = None,
          dob = None,
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = false
          )
        )
      }

      "return exception when index is greater than length of the list" in {
        val children = childrenService.createListOfChildren(requiredNumberOfChildren = 4)
        try {
          val result = childrenService.getChildById(5, children)
          result shouldBe a[Exception]
        } catch {
          case e: Exception =>
            e shouldBe a[Exception]
        }
      }

      "return exception when index is 0" in {
        val children = childrenService.createListOfChildren(requiredNumberOfChildren = 5)
        try {
          val result = childrenService.getChildById(5, children)
          result shouldBe a[Exception]
        } catch {
          case e: Exception =>
            e shouldBe a[Exception]
        }
      }

      "return exception when child list is empty" in {
        try {
          val result = childrenService.getChildById(5, List())
          result shouldBe a[Exception]
        } catch {
          case e: Exception =>
            e shouldBe a[Exception]
        }
      }
    }

    "retrieving ordinal number by index" should {

      "return first if index is 1" in {
        val result = childrenService.getOrdinalNumber(1)
        result shouldBe "first"
      }

      "return nineteenth if index is 19" in {
        val result = childrenService.getOrdinalNumber(19)
        result shouldBe "nineteenth"
      }

      "return NoSuchElementException when ordinal number is not present for an index" in {
        try {
          val result = childrenService.getOrdinalNumber(33)
          result shouldBe a[NoSuchElementException]
        } catch {
          case e: Exception =>
            e shouldBe a[Exception]
        }
      }
    }

    "replacing child in a list" should {

      "replace a child in a list" in {
        val childrenList = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            childCareCost = None,
            dob = None,
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
            childCareCost = None,
            dob = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 4,
            name = "Child 4",
            childCareCost = None,
            dob = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )

        val modifiedChild = _root_.models.child.Child(
          id = 3,
          name = "Child 3",
          childCareCost = None,
          dob = Some(LocalDate.now()),
          disability = Disability(
            disabled = false,
            severelyDisabled = false,
            blind = false,
            nonDisabled = true
          )
        )

        val result = childrenService.replaceChildInAList(childrenList,3 , modifiedChild)

        val modifiedChildrenList = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = None,
            childCareCost = None,
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
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 3,
            name = "Child 3",
            childCareCost = None,
            dob = Some(LocalDate.now()),
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = true
            )
          ),
          _root_.models.child.Child(
            id = 4,
            name = "Child 4",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )

        result shouldBe modifiedChildrenList
      }

    }

    "assessing children" should {

      "(remaining) determine if we have remaining children we need to collect details from" in {
        val children = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = None,
            childCareCost = None,
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
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.hasRemainingChildren(children = children, currentIndex = 1)
        result shouldBe true
      }

      "(non remaining) determine if we have remaining children we need to collect details from" in {
        val children = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = None,
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.hasRemainingChildren(children = children, currentIndex = 1)
        result shouldBe false
      }
    }

    "Child Age" should {

      "return true if child age is less than 15 years on 1st sep without disabled" in {
        val ageAsOfSept = 14
        val disabled = false

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe true
      }

      "return true if child age is less than 15 years on 1st sep  with disabled" in {
        val ageAsOfSept = 14
        val disabled = true

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe true
      }

      "return false if child age is equal to 15 years on 1st sep with disabled" in {
        val ageAsOfSept = 15
        val disabled = true

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe true
      }

      "return false if child age is equal to 15 years on 1st sep  without disabled" in {
        val ageAsOfSept = 15
        val disabled = false

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe false
      }

      "return false if child age is greater than 15 years on 1st sep  without disabled" in {
        val ageAsOfSept = 16
        val disabled = false

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe false
      }

      "return false if child age is greater than 15 years on 1st sep  with disabled" in {
        val ageAsOfSept = 16
        val disabled = true

        childrenService.childIsLessThan15Or16WhenDisabled(ageAsOfSept, disabled) shouldBe false
      }

      "return true if child age is less than 20" in {
        val todaysAge = 16

        childrenService.childIsLessThan20YearsOld(todaysAge) shouldBe true
      }

      "return false if child age is greater than or equal to 20" in {
        val todaysAge = 20

        childrenService.childIsLessThan20YearsOld(todaysAge) shouldBe false
      }

      "return true if child age is equal to 15 on 1st sep" in {
        val ageAsOfSept = 15

        childrenService.childIsAgedBetween15And16(ageAsOfSept) shouldBe true
      }

      "return false if child age is greater than 16 on 1st sep" in {
        val ageAsOfSept = 16

        childrenService.childIsAgedBetween15And16(ageAsOfSept) shouldBe false
      }

      "return true if child age is between 2 and 4" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth = LocalDate.parse("2013-05-31", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe true
      }

      "return false if child age is below 2" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth = LocalDate.parse("2015-05-31", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe false
      }

      "return false if child age is above 4" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth = LocalDate.parse("2010-05-31", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 1,
            name = "Child 1",
            dob = Some(dateOfBirth),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe false
      }

      "return false when dob in the list of children is not in between 2 and 4" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth1 = LocalDate.parse("2010-05-31", formatter)

        val dateOfBirth2 = LocalDate.parse("2015-08-26", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 0,
            name = "Child 1",
            dob = Some(dateOfBirth1),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 1,
            name = "Child 2",
            dob = Some(dateOfBirth2),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe false
      }

      "return true when dob of one of the child in list of children is between 2 and 4" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth1 = LocalDate.parse("2013-05-31", formatter)

        val dateOfBirth2 = LocalDate.parse("2015-08-26", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 0,
            name = "Child 1",
            dob = Some(dateOfBirth1),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 1,
            name = "Child 2",
            dob = Some(dateOfBirth2),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe true
      }

      "return true when dob in list of children is between 2 and 4" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val dateOfBirth1 = LocalDate.parse("2012-05-31", formatter)

        val dateOfBirth2 = LocalDate.parse("2013-08-26", formatter)

        val children = List(
          _root_.models.child.Child(
            id = 0,
            name = "Child 1",
            dob = Some(dateOfBirth1),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          ),
          _root_.models.child.Child(
            id = 1,
            name = "Child 2",
            dob = Some(dateOfBirth2),
            childCareCost = None,
            disability = Disability(
              disabled = false,
              severelyDisabled = false,
              blind = false,
              nonDisabled = false
            )
          )
        )
        val result = childrenService.childBenefitsEligibility(children)
        result shouldBe true
      }

    }

    "esc Eligibility" should {

      "return true when child is qualifying for ESC" in {
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


        val escEligibility = ESCEligibilityOutput(List(TaxYear(
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
          ))))
        val result = childrenService.getEscEligibility(escEligibility)
        result shouldBe true
      }

      "return false when child is not qualifying for ESC" in {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val periodStart = LocalDate.parse("2016-06-20", formatter)
        val periodEnd = LocalDate.parse("2017-04-06", formatter)

        val child = ESCOutputChild(
          id = 0,
          name = Some("Child 1"),
          qualifying = false,
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


        val escEligibility = ESCEligibilityOutput(List(TaxYear(
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
          ))))
        val result = childrenService.getEscEligibility(escEligibility)
        result shouldBe false
      }

    }

  }
}
