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

import config.ApplicationConfig
import models.claimant.Income
import models.payload.calculator.input.esc.ESCEligibility
import models.payload.eligibility.output.EligibilityOutput
import models.payload.eligibility.output.esc.{ESCOutputClaimant, ESCPeriod}
import org.joda.time.LocalDate
import play.api.Logger

/**
 * Created by user on 30/03/16.
 */

trait PayloadESCCalculatorService {

   def createESCCalculatorPayload(eligibilityOutput: EligibilityOutput, childrenInput : List[_root_.models.child.Child], claimants : List[_root_.models.claimant.Claimant]): ESCEligibility = {
     Logger.debug(s"PayloadESCCalculatorService.createESCCalculatorPayload")
     val escEligibilityOutput = eligibilityOutput.eligibility.esc.get
     val escTaxYears = createESCTaxYears(escEligibilityOutput.taxYears, childrenInput, claimants)
     ESCEligibility(
       escTaxYear = escTaxYears
     )
   }

  private def createESCTaxYears(escEligibilityOutputTaxYears: List[models.payload.eligibility.output.esc.TaxYear], childrenInput : List[_root_.models.child.Child], claimants : List[_root_.models.claimant.Claimant]) = {
    Logger.debug(s"PayloadESCCalculatorService.createESCTaxYears")
    for(escTaxYear <- escEligibilityOutputTaxYears) yield {
      val escPeriods = createESCPeriods(escTaxYear.periods, childrenInput, claimants)
      models.payload.calculator.input.esc.ESCTaxYear(
        startDate = escTaxYear.from,
        endDate = escTaxYear.until,
        periods = escPeriods
      )
    }

  }

   private def createESCPeriods(periods : List[ESCPeriod], childrenInput : List[_root_.models.child.Child], claimants : List[_root_.models.claimant.Claimant]) = {
     Logger.debug(s"PayloadESCCalculatorService.createESCPeriods")
     for (period <- periods) yield {
       val escClaimants = createESCClaimants(period.claimants, childrenInput, claimants)
       models.payload.calculator.input.esc.ESCPeriod (
         from = period.from,
         until = period.until,
         claimants = escClaimants
       )
     }
   }

  private def createESCClaimants(escClaimants: List[ESCOutputClaimant], childrenInput : List[_root_.models.child.Child], claimants : List[_root_.models.claimant.Claimant]) = {
    Logger.debug(s"PayloadESCCalculatorService.createESCClaimants")
    for(escClaimant <- escClaimants) yield {
      val claimant = if(escClaimant.isPartner) {
        claimants.tail.head
      } else {
        claimants.head
      }

      val previousIncome = getPreviousIncomes(claimant.previousIncome)
      val escIncome = createESCIncome(previousIncome, claimant.currentIncome)
      val escClaimantElements = createESCClaimantElements(escClaimant.elements)
      val escChildCareAmount = calculateAmount(childrenInput)
      models.payload.calculator.input.esc.ESCClaimant(
        qualifying = escClaimant.qualifying,
        isPartner = escClaimant.isPartner,
        eligibleMonthsInPeriod = escClaimant.eligibleMonthsInPeriod,
        income = escIncome,
        elements = escClaimantElements,
        escStartDate = LocalDate.now(), //at present today's date - later based on user journey question wil be asked to user for start date
        escAmount = if (escClaimants.size > 1 && escClaimants.head.qualifying && escClaimants.tail.head.qualifying) {
          escChildCareAmount/2
        } else escChildCareAmount, //childcare cost of all children of the claimants
        escAmountPeriod = childrenInput.head.childCareCostPeriod
      )
    }
  }

  private def calculateAmount(childrenInput : List[_root_.models.child.Child]): BigDecimal = {
    Logger.debug(s"PayloadESCCalculatorService.calculateAmount")
    childrenInput.foldLeft(BigDecimal(0.00))((amount, child) => {
      child.childCareCost match {
        case Some(cost) => amount + cost
        case _ => amount
      }
    })
  }

  private def createESCClaimantElements(elements: models.payload.eligibility.output.esc.ClaimantElements) = {
    Logger.debug(s"PayloadESCCalculatorService.createESCClaimantElements")
    models.payload.calculator.input.esc.ESCElements (
      vouchers  = elements.vouchers
    )
  }

  private def createESCIncome(previousIncome : Tuple2[BigDecimal, BigDecimal], currentIncome: Option[Income]) = {
    Logger.debug(s"PayloadESCCalculatorService.createESCIncome")
    val incomes = currentIncome match {
      case Some(x) =>

        val employmentIncome = x.employmentIncome match {
          case Some(x) => x
          case _ => previousIncome._1
        }

        val pension = x.pension match {
          case Some(x) => x
          case _ => previousIncome._2
        }
        (employmentIncome, pension)
      case _ =>
        //if the user as selected is your current income likely to change as No then take previousIncome has your current income.
        (previousIncome._1, previousIncome._2)
    }

    models.payload.calculator.input.esc.ESCIncome(
      taxablePay = (incomes._1 - (incomes._2 * ApplicationConfig.noOfMonths)), //employment income minus pension
      gross = incomes._1, //employment income
      taxCode = "", //blank as of now - backend will manipulate to default value based on tax year
      niCategory = "" //blank as of now - backend will manipulate to default value based on tax year
    )
  }

  private def getPreviousIncomes(previousIncome: Option[Income]) = {
    Logger.debug(s"PayloadESCCalculatorService.getPreviousIncomes")
    previousIncome match {
      case Some(x) => ( getIncomeValue(x.employmentIncome) , getIncomeValue(x.pension))
      case _ =>   (BigDecimal(0.00), BigDecimal(0.00))
    }
  }

  private def getIncomeValue(value : Option[BigDecimal]): BigDecimal = {
    Logger.debug(s"PayloadESCCalculatorService.getIncomeValue")
    value match {
      case Some(x) => x
      case _ => BigDecimal(0.00)
    }
  }

 }
