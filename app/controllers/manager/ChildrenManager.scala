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

package controllers.manager

import _root_.models.child.Disability
import config.ApplicationConfig
import models.payload.eligibility.output.esc.ESCEligibilityOutput
import org.joda.time.LocalDate
import play.api.Logger

import scala.annotation.tailrec

/**
 * Created by adamconder on 08/02/2016.
 */
trait ChildrenManager  {

  val childrenService = new ChildrenService

  class ChildrenService extends HelperManager {

    private val ordinalNumber =  Map( "1"-> "first", "2" -> "second", "3" -> "third", "4"-> "fourth", "5"-> "fifth", "6" -> "sixth", "7" -> "seventh", "8"-> "eight",
      "9"-> "ninth", "10" -> "tenth", "11" -> "eleventh", "12"-> "twelfth", "13"-> "thirteenth", "14" -> "fourteenth", "15" -> "fifteenth", "16"-> "sixteenth",
      "17"-> "seventeenth", "18" -> "eighteenth", "19" -> "nineteenth")


    private def createChild(index: Int) = {
      Logger.debug(s"ChildrenManager.createChild")
      _root_.models.child.Child(
        id = index.toShort,
        name = s"Child $index",
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

    def replaceChildInAList(children: List[models.child.Child], index: Int, modifiedChild: models.child.Child):
    List[models.child.Child] = children.patch(index-1, Seq(modifiedChild), 1)

    def getOrdinalNumber(index: Int): String ={
      Logger.debug(s"ChildrenManager.getOrdinalNumber")
      ordinalNumber.get(index.toString) match  {
        case Some(x) => x
        case _ => throw new NoSuchElementException
      }
    }

    def getChildById(index: Int, children : List[models.child.Child]) = {
      Logger.debug(s"ChildrenManager.getChildById")
      Logger.debug(s"###children ;::: $children")
      try {
        val child = children.filter(c => c.id == index.toShort).head
        Logger.debug(s"ChildManager getChildById: $child")
        child
      }
      catch {
        case e : Exception =>
          throw e
      }
    }

    def createListOfChildren(requiredNumberOfChildren: Int) : List[models.child.Child] = {
      Logger.debug(s"ChildrenManager.createListOfChildren")
      val children = for (i <- 1 to requiredNumberOfChildren) yield {
        val index = i
        val child = createChild(index)
        child
      }
      children.toList
    }

    def modifyListOfChildren(requiredNumberOfChildren : Int, children : List[models.child.Child]) = {
      Logger.debug(s"ChildrenManager.modifyListOfChildren")
      val numberOfChildren = children.size
      val difference = requiredNumberOfChildren - numberOfChildren

      @tailrec
      def modifyListOfChildrenHelper (children : List[models.child.Child], remaining : Int) : List[models.child.Child] = {
        Logger.debug(s"ChildrenManager.modifyListOfChildrenHelper")
        val sorted = children.sortBy(x => x.id)

        remaining match {
          case x if remaining == 0 =>
            Logger.debug(s"same: difference: $remaining, x: $x")
            // no difference children left
            sorted
          case x if remaining > 0 =>
            Logger.debug(s"add: difference: $remaining, x: $x")
            // we need to add another child
            val index = sorted.last.id + 1
            Logger.debug(s"index: $index")
            val child = createChild(index)
            val modified = child :: sorted
            modifyListOfChildrenHelper(modified, x - 1)
          case x if remaining < 0 =>
            Logger.debug(s"remove: difference: $remaining, x: $x")
            // we need to remove a child
            val lastId = sorted.last.id - 1
            val modified = sorted.splitAt(lastId)._1
            modifyListOfChildrenHelper(modified, x + 1)
        }
      }

      if (difference == 0) {
        children
      } else {
        modifyListOfChildrenHelper(children, difference)
      }
    }

    def hasRemainingChildren(children: List[models.child.Child], currentIndex : Int) : Boolean = {
      Logger.debug(s"ChildrenManager.hasRemainingChildren")
      val lastIndex = children.sortBy(c => c.id).last.id
      Logger.debug(s"\n\n lastIndex $lastIndex currentIndex: $currentIndex \n\n")
      currentIndex < lastIndex
    }

    def validateChildDob(dob: Option[LocalDate]) : Boolean = {
      Logger.debug(s"ChildrenManager.validateChildDob")
      val childAge = age(dob)
      if (childAge.>=(ApplicationConfig.freeEntitlementAgeLowerLimit) && childAge.<=(ApplicationConfig.freeEntitlementAgeUpperLimit))
        true
      else
        false
    }

    def childBenefitsEligibility(children: List[models.child.Child]) : Boolean = {
      Logger.debug(s"ChildrenManager.childBenefitsEligibility")
      !children.filter(x => validateChildDob(x.dob)).isEmpty
    }

   def childIsLessThan15Or16WhenDisabled(childAge1stSept : Int, disabled: Boolean): Boolean = {
     Logger.debug(s"ChildrenManager.childIsLessThan15Or16WhenDisabled")
     if(disabled)
       childAge1stSept < ApplicationConfig.maxChildAge16DisabledLimit
     else
       childAge1stSept < ApplicationConfig.maxChildAge15Limit
   }

    def childIsLessThan20YearsOld(childAgeToday : Int): Boolean = {
      Logger.debug(s"ChildrenManager.childIsLessThan20YearsOld")
      childAgeToday < ApplicationConfig.maxChildAge20Limit
    }

    def childIsAgedBetween15And16(childAge1stSept : Int): Boolean = {
      Logger.debug(s"ChildrenManager.childIsAgedBetween15And16")
      childAge1stSept >= ApplicationConfig.maxChildAge15Limit && childAge1stSept < ApplicationConfig.maxChildAge16DisabledLimit
    }

    def getEscEligibility(escEligibilityResult : ESCEligibilityOutput) = {
      Logger.debug(s"ChildrenManager.getEscEligibility")
      val escEligibilityChild = escEligibilityResult.taxYears.exists(taxYears => taxYears.periods.exists(periods => periods.children.exists(children => if(children.qualifying) true else false)))
      (escEligibilityChild)

    }


  }
}
