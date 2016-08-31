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

import models.payload.eligibility.output.esc.ESCEligibilityOutput
import play.api.Logger

import scala.annotation.tailrec

/**
 * Created by adamconder on 18/02/2016.
 */
trait ClaimantManager {

  val claimantService = new ClaimantService

  class ClaimantService {

    private def createClaimant(index: Int) = {
      Logger.debug(s"ClaimantManager.createClaimant")
      _root_.models.claimant.Claimant(
        id = index.toShort,
        disability = _root_.models.claimant.Disability(
          disabled = false,
          severelyDisabled = false,
          incomeBenefits = false,
          carersAllowance = false,
          noBenefits = false
        ),
        previousIncome = None,
        currentIncome = None,
        hours = None,
        whereDoYouLive = None,
        doYouLiveWithPartner = None,
        escVouchersAvailable = None
      )
    }

    def createListOfClaimants(requiredNumberOfClaimants: Int = 1) : List[_root_.models.claimant.Claimant] = {
      Logger.debug(s"ClaimantManager.createListOfClaimants")
      val claimants = for (i <- 1 to requiredNumberOfClaimants) yield {
        val index = i
        val claimant = createClaimant(index)
        claimant
      }
      claimants.toList
    }

    def dropClaimantAtIndex(claimants : List[_root_.models.claimant.Claimant], index : Int) : List[_root_.models.claimant.Claimant] = {
      Logger.debug(s"ClaimantManager.dropClaimantAtIndex")
      val indexAtPosition = index - 1
      val lastIndex = claimants.sortBy(c => c.id).last.id
      Logger.debug(s"index at position: $indexAtPosition length: $lastIndex")
      if (indexAtPosition < lastIndex) {
        val split = claimants.splitAt(indexAtPosition)
        val modified = split._1 ::: split._2.tail
        modified
      } else {
        claimants
      }
    }

    def modifyListOfClaimants(claimants : List[_root_.models.claimant.Claimant], requiredNumberOfClaimants : Int) : List[_root_.models.claimant.Claimant] = {
      Logger.debug(s"ClaimantManager.modifyListOfClaimants")
      val numberOfClaimants = claimants.size
      val difference = requiredNumberOfClaimants - numberOfClaimants

      @tailrec
      def modifyListOfClaimantsHelper(claimants : List[_root_.models.claimant.Claimant], remaining: Int) : List[_root_.models.claimant.Claimant] = {
        val sorted = claimants.sortBy(x => x.id)
        remaining match {
          case x if remaining == 0 =>
            Logger.debug(s"same: difference: $remaining, x: $x")
            // no difference
            sorted
          case x if remaining > 0 =>
            Logger.debug(s"add: difference: $remaining, x: $x")
            val index = sorted.last.id + 1
            val claimant = createClaimant(index = index)
            val modified = claimant :: sorted
            modifyListOfClaimantsHelper(modified, x - 1)
          case x if remaining < 0 =>
            Logger.debug(s"remove: difference: $remaining, x: $x")
            val lastId = sorted.last.id - 1
            val modified = sorted.splitAt(lastId)._1
            modifyListOfClaimantsHelper(modified, x + 1)
        }
      }

      modifyListOfClaimantsHelper(claimants, difference)
    }

    def getClaimantById(claimants: List[_root_.models.claimant.Claimant], index : Int) = {
      Logger.debug(s"ClaimantManager.getClaimantById")
      try {
        claimants.filter(x => x.id == index.toShort).head
      } catch {
        case e : Exception =>
          Logger.warn(s"ClaimantManager.getClaimantById exception : ${e.getMessage}")
          throw e
      }
    }

    def escVouchersAvailable(claimant : _root_.models.claimant.Claimant) : Boolean = {
      Logger.debug(s"ClaimantManager.escVouchersAvailable")
      claimant.escVouchersAvailable match {
        case Some(x) => x match {
          case "Yes" => true
          case "No" => false
          case "notSure" => true
        }
        case _ => false
      }
    }


    def getEscEligibility(escEligibilityResult : ESCEligibilityOutput) = {

      val escEligibilityParent = escEligibilityResult.taxYears.exists(taxYears => taxYears.periods.exists(periods => periods.claimants.exists(claimant => if(!claimant.isPartner && claimant.qualifying) true else false)))
      val escEligibilityPartner = escEligibilityResult.taxYears.exists(taxYears => taxYears.periods.exists(periods => periods.claimants.exists(claimant => if(claimant.isPartner && claimant.qualifying) true else false)))
      (escEligibilityParent, escEligibilityPartner)

    }

    def copyPageModelToClaimantDisability(pageModel : _root_.models.pages.ClaimantBenefitsPageModel, index : Short) : _root_.models.claimant.Disability ={
      Logger.debug(s"ClaimantManager.copyPageModelToClaimantDisability")
      val disability = _root_.models.claimant.Disability(
        disabled = pageModel.disabilityBenefit,
        severelyDisabled = pageModel.severeDisabilityBenefit,
        incomeBenefits = pageModel.incomeBenefit,
        carersAllowance = pageModel.carerAllowanceBenefit,
        noBenefits = pageModel.noBenefit
      )
      disability
    }

    def replaceClaimantInAList(claimants: List[_root_.models.claimant.Claimant], claimant :_root_.models.claimant.Claimant, index : Int) :
    List[_root_.models.claimant.Claimant] = claimants.patch(index-1, Seq(claimant), index)

  }
}
