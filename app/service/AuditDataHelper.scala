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

import scala.annotation.tailrec

/**
 * Created by user on 05/08/16.
 */
object AuditDataHelper extends AuditDataHelper

trait AuditDataHelper {

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

  def getResultSummaryAuditData(auditData : Tuple10[String, String, String, String, String, String, String, String, String, String], claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child]) : Map[String, String] = {
    val escPartnerEligibility = if(claimants.size == 2 )  auditData._9 else ""
    val singleParent = if(claimants.size == 1 ) true else false
    val location = if (claimants.head.whereDoYouLive.isDefined) claimants.head.whereDoYouLive.get.toString else ""

    val childCareCost = getChildCareCost(children)
    Map("numberOfChildren" -> (children.size).toString,"tfcAmount" -> auditData._1, "tcAmount" ->  auditData._2, "escAmount" ->  auditData._3,
      "tcAmountByUser" ->  auditData._4, "ucAmountByUser" ->  auditData._5, "tfcEligibility" ->
        auditData._6,"tcEligibility" ->  auditData._7, "escEligibilityParent" ->  auditData._8,
      "escEligibilityPartner" ->  escPartnerEligibility,"user-single" -> singleParent.toString,
      "user-double" -> (!singleParent).toString, "location" -> location, "annualChildCareCost" -> auditData._10) ++ childCareCost
  }

}
