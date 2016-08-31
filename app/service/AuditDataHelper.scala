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

import play.api.Logger
import controllers.manager.{HelperManager, ClaimantManager}
import scala.annotation.tailrec

/**
 * Created by user on 05/08/16.
 */
object AuditDataHelper extends AuditDataHelper with ClaimantManager with HelperManager

trait AuditDataHelper extends ClaimantManager with HelperManager{

  private def getChildCareCost(children: List[_root_.models.child.Child]) : Map[String, String] = {

    @tailrec
    def costs(children: List[_root_.models.child.Child], acc: Map[String, String], count : Int) : Map[String, String] = {
      children match {
        case Nil =>
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          acc
        // $COVERAGE-ON

        case head :: tail =>
          val cost = if(head.childCareCost.isDefined) head.childCareCost.get.toString else ""
          costs(tail, acc ++ Map(s"Child${count}Cost" -> cost), count+1)
      }
    }
    val costMap = costs(children, Map(""->""), 1)
    costMap.filter(_._1 != "")
  }

  def getResultSummaryAuditData(auditData : Tuple10[String, String, String, String, String,
    String, String, String, String, String], claimants : List[_root_.models.claimant.Claimant],
                                children: List[_root_.models.child.Child]) : Map[String, String] = {

    Logger.debug(s"AuditDataHelper.getResultSummaryAuditData")

    val escPartnerEligibility = if(claimants.size == 2 )  auditData._9 else ""
    val singleParent = if(claimants.size == 1 ) true else false
    val location = if (claimants.head.whereDoYouLive.isDefined) claimants.head.whereDoYouLive.get.toString else ""

    val childCareCost = getChildCareCost(children)
    Map("numberOfChildren" -> (children.size).toString,"tfcAmount" -> auditData._1, "tcAmount" ->  auditData._2, "escAmount" ->  auditData._3,
      "tcAmountByUser" ->  auditData._4, "ucAmountByUser" ->  auditData._5, "tfcEligibility" ->
        auditData._6,"tcEligibility" ->  auditData._7, "escEligibilityParent" ->  auditData._8,
      "escEligibilityPartner" ->  escPartnerEligibility,"user-single" -> singleParent.toString,
      "user-couple" -> (!singleParent).toString, "location" -> location,
      "parentPreviousIncome" -> getAnnualIncomes(claimants.head.previousIncome),
      "parentCurrentIncome" -> getAnnualIncomes(claimants.head.currentIncome),
      "partnerPreviousIncome" -> {if(!singleParent) getAnnualIncomes(claimants.tail.head.previousIncome) else ""},
      "partnerCurrentIncome" -> {if(!singleParent) getAnnualIncomes(claimants.tail.head.currentIncome) else ""},
      "annualChildCareCost" -> auditData._10) ++ childCareCost
  }

  private def getChildrenBenefits(children: List[_root_.models.child.Child]) : Map[String, String] = {

    @tailrec
    def benefits(children: List[_root_.models.child.Child], acc: Map[String, String], count : Int) : Map[String, String] = {
      children match {
        case Nil =>
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          acc
        // $COVERAGE-ON



        case head :: tail =>
          val disability = head.disability
          benefits(tail, acc ++ Map(s"Child${count}Disabled" -> disability.disabled.toString,
            s"Child${count}SeverelyDisabled" -> disability.severelyDisabled.toString,
            s"Child${count}Blind" -> disability.blind.toString,
            s"Child${count}NoBenefit" -> disability.nonDisabled.toString
          ),
            count+1)
      }
    }
    val childBenefitsMap = benefits(children, Map(""->""), 1)
    childBenefitsMap.filter(_._1 != "")
  }

  def getClaimantChildrenBenefitsAuditData(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child]) : Map[String, String] = {

    Logger.debug(s"AuditDataHelper.getClaimantChildrenBenefitsAuditData")
    val parentDisability = claimants.head.disability
    val partnerDisability = if (claimants.size == 2)  {
      val partnerDisability = claimants.tail.head.disability
      (partnerDisability.disabled.toString, partnerDisability.severelyDisabled.toString,
        partnerDisability.incomeBenefits.toString, partnerDisability.carersAllowance.toString,
        partnerDisability.noBenefits.toString)
    }
    else  ("","","","","")


    Map("parentDisabled" -> parentDisability.disabled.toString,
      "parentSeverelyDisabled" -> parentDisability.severelyDisabled.toString,
      "parentIncomeBenefits" -> parentDisability.incomeBenefits.toString,
      "parentCarersAllowance" -> parentDisability.carersAllowance.toString,
      "parentNoBenefits" -> parentDisability.noBenefits.toString,
      "partnerDisabled" -> partnerDisability._1 , "partnerSeverelyDisabled" -> partnerDisability._2,
      "partnerIncomeBenefits" -> partnerDisability._3,
      "partnerCarersAllowance" -> partnerDisability._4,"partnerNoBenefits" -> partnerDisability._5) ++ getChildrenBenefits(children)

  }

  private def getChildcareCostPerAge(children : List[_root_.models.child.Child]) = {
    @tailrec
    def costPerAge(children: List[_root_.models.child.Child], acc: Map[String, String]) : Map[String, String] = {
      children match {
        case Nil => acc
        case head :: tail =>
          val childAge = age(head.dob)
          val finalAge = if (childAge < 1 ) 0 else childAge
          val cost = head.childCareCost
          if (cost.isDefined) {
            val value = acc.get(s"Child$finalAge")
            val childcareCost = if (value.isDefined)
                s"${value.get.toString},${cost.get}"
              else
                head.childCareCost.get.toString()

            costPerAge(tail, acc ++ Map(s"Child$finalAge" -> childcareCost))
          }
          else costPerAge(tail, acc)
      }
    }
    costPerAge(children, Map("" -> "")).filter(_._1 !="")
  }

  def getChildcareCostPerAgeAuditData(children : List[_root_.models.child.Child]) : Map[String, String] = {
    getChildcareCostPerAge(children)
  }

  private def getAnnualIncomes(income : Option[_root_.models.claimant.Income]) = {
    income match {
      case Some(x) => (claimantService.getIncomeValue(x.employmentIncome) + claimantService.getIncomeValue(x.otherIncome)).toString()
      case _ => "0"
    }

  }


}
