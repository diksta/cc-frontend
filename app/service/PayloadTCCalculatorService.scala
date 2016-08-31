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

import models.payload.eligibility.output.EligibilityOutput
import models.payload.calculator.input.tc._
import models.payload.eligibility.output.tc.{TCOutputClaimant, TCOutputChild, HouseholdElements, TCPeriod}
import play.api.Logger

/**
* Created by user on 24/03/16.
*/

trait PayloadTCCalculatorService {

  def createTCCalculatorPayload(eligibilityOutput: EligibilityOutput, claimants : List[_root_.models.claimant.Claimant]): TCEligibility = {
    Logger.debug(s"PayloadTCCalculatorService.createTCCalculatorPayload")
    val tcEligibilityOutput = eligibilityOutput.eligibility.tc.get
    val taxYears = createTCTaxYear(tcEligibilityOutput.taxYears, claimants)
    TCEligibility(
      taxYears = taxYears,
      proRataEnd = taxYears.head.from.plusYears(1)
    )
  }

  private def createTCTaxYear(taxYears : List[models.payload.eligibility.output.tc.TaxYear], claimants : List[_root_.models.claimant.Claimant]) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCTaxYear")
    for (taxYear <- taxYears) yield {
      TaxYear(
        from = taxYear.from,
        until = taxYear.until,
        houseHoldIncome = taxYear.houseHoldIncome,
        periods = createTCPeriod(taxYear.periods, claimants)
      )
    }
  }

  private def createTCPeriod(periods : List[TCPeriod], claimants : List[_root_.models.claimant.Claimant]) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCPeriod")
    for (period <- periods) yield {
      val tcChildren = createTCChildren(period.children)
      val tcClaimants = createTCClaimants(period.claimants, claimants)
      val houseHoldElement = createTCHouseHoldElements(period.householdElements)

      Period (
        from = period.from,
        until = period.until,
        householdElements = houseHoldElement,
        claimants = tcClaimants,
        children = tcChildren
      )
    }
  }

  private def createTCClaimants(tcEligibilityOutputClaimants : List[TCOutputClaimant], claimants : List[_root_.models.claimant.Claimant]) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCClaimants")
    for(tcEligibilityOutputClaimant <- tcEligibilityOutputClaimants) yield {
      val claimantElements = createTCClaimantElements(tcEligibilityOutputClaimant.claimantDisability)
      Claimant(
        qualifying = tcEligibilityOutputClaimant.qualifying,
        isPartner = tcEligibilityOutputClaimant.isPartner,
        claimantElements = claimantElements,
        doesNotTaper = tcEligibilityOutputClaimant.isPartner match {
          case false => isTaperingRequiredForClaimants(claimants.head)
          case true => isTaperingRequiredForClaimants(claimants.tail.head)
        },
        failures = Some(List())
      )
    }
  }

  private def isTaperingRequiredForClaimants(claimant : _root_.models.claimant.Claimant) : Boolean = {
    Logger.debug(s"PayloadTCCalculatorService.isTaperingRequiredForClaimants")
    claimant.disability.incomeBenefits
  }

  private def createTCClaimantElements(claimantDisability: models.payload.eligibility.output.tc.ClaimantDisability) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCClaimantElements")
    ClaimantDisability(
      disability = claimantDisability.disability,
      severeDisability = claimantDisability.severeDisability
    )
  }

  private def createTCChildren(children : List[TCOutputChild]) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCChildren")
    for(child <- children) yield {
      val tcChildElement = createTCChildElements(child.childElements)
          Child(
          id = child.id,
          name = child.name.get,
          qualifying = child.qualifying,
          childcareCost = child.childcareCost,
          childcareCostPeriod = child.childcareCostPeriod,
          childElements = tcChildElement
      )
    }
  }

  private def createTCChildElements(childElements : models.payload.eligibility.output.tc.ChildElements) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCChildElements")
    ChildElements(
      child = childElements.child,
      youngAdult = childElements.youngAdult,
      disability = childElements.disability,
      severeDisability = childElements.severeDisability,
      childcare = childElements.childcare
    )
  }

  private def createTCHouseHoldElements(houseHoldElements : HouseholdElements) = {
    Logger.debug(s"PayloadTCCalculatorService.createTCHouseHoldElements")
    HouseHoldElements(
      basic = houseHoldElements.basic,
      hours30 = houseHoldElements.hours30,
      childcare = houseHoldElements.childcare,
      loneParent = houseHoldElements.loneParent,
      secondParent = houseHoldElements.secondParent,
      family = houseHoldElements.family
    )
  }
}
