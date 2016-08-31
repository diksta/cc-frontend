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

import java.util.Calendar

import config.ApplicationConfig
import controllers.manager.ClaimantManager
import models.claimant.Income
import models.payload.eligibility.input.esc.{ESCPayload, ESCEligibility}
import models.payload.eligibility.input._
import models.payload.eligibility.input.tc.{TCPayload, TCEligibility}
import models.payload.eligibility.input.tfc.{TFCPayload, TFC, TFCEligibility}
import mappings.Periods

import org.joda.time.LocalDate
import play.api.Logger

/**
 * Created by user on 17/03/16.
 */
object PayloadEligibilityService extends PayloadEligibilityService

trait PayloadEligibilityServiceHelper {

  def determineApril6DateFromNow(from: LocalDate) : LocalDate = {
    Logger.debug(s"PayloadEligibilityServiceHelper.determineApril6DateFromNow")
    val currentCalendar = Calendar.getInstance()
    currentCalendar.clear()
    currentCalendar.setTime(from.toDate)
    val periodYear = currentCalendar.get(Calendar.YEAR)
    val periodStart = from.toDate

    val januaryCalendar = Calendar.getInstance()
    januaryCalendar.clear()
    januaryCalendar.set(Calendar.YEAR, periodYear)
    januaryCalendar.set(Calendar.MONTH, Calendar.JANUARY)
    januaryCalendar.set(Calendar.DAY_OF_MONTH, 1)
    val january1st = januaryCalendar.getTime

    val aprilCalendarCurrentYear = Calendar.getInstance()
    aprilCalendarCurrentYear.clear()
    aprilCalendarCurrentYear.set(Calendar.YEAR, periodYear)
    aprilCalendarCurrentYear.set(Calendar.MONTH, Calendar.APRIL)
    aprilCalendarCurrentYear.set(Calendar.DAY_OF_MONTH, 6)
    val april6CurrentYear = aprilCalendarCurrentYear.getTime

    val aprilCalendarNextYear = Calendar.getInstance()
    aprilCalendarNextYear.clear()
    currentCalendar.setTime(april6CurrentYear)
    aprilCalendarNextYear.set(Calendar.YEAR, periodYear+1)
    aprilCalendarNextYear.set(Calendar.MONTH, Calendar.APRIL)
    aprilCalendarNextYear.set(Calendar.DAY_OF_MONTH, 6)
    val april6NextYear = aprilCalendarNextYear.getTime

    if ((periodStart.compareTo(january1st) == 0 || periodStart.after(january1st)) && periodStart.before(april6CurrentYear)) {
      LocalDate.fromDateFields(april6CurrentYear)
    } else {
      LocalDate.fromDateFields(april6NextYear)
    }
  }
}

trait PayloadEligibilityService extends PayloadEligibilityServiceHelper with ClaimantManager {

  def createTCEligibilityPayload(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child]): TCPayload = {

    Logger.debug(s"PayloadEligibilityService.createTCEligibilityPayload")

    val taxYears = createTaxYears(claimants, children, tcScheme = true)

    TCPayload(eligibility = TCEligibility(taxYears))
  }

  def createESCEligibilityPayload(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child]): ESCPayload = {

    Logger.debug(s"PayloadEligibilityService.createESCEligibilityPayload")

    val taxYears = createTaxYears(claimants, children, tcScheme = false)

    ESCPayload(eligibility = ESCEligibility(taxYears))
  }

  def createTFCEligibilityPayload(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child]): TFCPayload = {

    Logger.debug(s"PayloadEligibilityService.createTFCEligibilityPayload")

    val claimantList = createClaimants(claimants)
    val childList = createChildren(children)

    val tfc = TFC(
      from = LocalDate.now(),
      numberOfPeriods = 4, //hardcoded to 4 for 12 months period
      claimants = claimantList,
      children = childList
    )
   TFCPayload(eligibility = TFCEligibility(tfc = tfc))
  }

  private def createTaxYears(claimants : List[_root_.models.claimant.Claimant], children: List[_root_.models.child.Child], tcScheme : Boolean): List[TaxYear] = {

    Logger.debug(s"PayloadEligibilityService.createTaxYears")

    val currentYearClaimantList = createClaimants(claimants)
    val nextYearClaimantList = for(claimant <- currentYearClaimantList) yield {
      val currentYearTotalIncome = claimant.totalIncome
      claimant.copy(previousTotalIncome = currentYearTotalIncome)
    }
    val childList = createChildren(children)
    val now = LocalDate.now()
    val april6thCurrentYear = determineApril6DateFromNow(now)

    //for tc scheme the until date for next tax year is end of the tax year but for esc it is just today's date plus one year
    val secondTaxYearUntilDate = tcScheme match {
      case true => april6thCurrentYear.plusYears(1)
      case _ => LocalDate.now().plusYears(1)
    }

    List(TaxYear(
      from = now,
      until = april6thCurrentYear,
      claimants = currentYearClaimantList,
      children = childList
    ),
      TaxYear(
        from = april6thCurrentYear,
        until = secondTaxYearUntilDate,
        claimants = nextYearClaimantList,
        children = childList
      ))
  }

  private def createChildren(children: List[_root_.models.child.Child]) : List[Child] = {

    Logger.debug(s"PayloadEligibilityService.createChildren")

    for(child <- children) yield {

      // the disability details map to the child details screen
      val disability = Disability(
        disabled = child.disability.blind || child.disability.disabled,
        severelyDisabled = child.disability.severelyDisabled
      )

      //derived from Cost and education screen. if child above 16 then education question is asked to the user.
      val education = child.education match {

        case Some(x) =>
          x.inEducation match {
            case true  =>  Some(Education(
              inEducation = x.inEducation,
              startDate = x.startDate.get
            ))
            case _ => None
          }

        case _ => None
      }

      val childCareCost = child.childCareCost match {
        case Some(x) => x
        case _ => BigDecimal(0.00)
      }

      Child  (
        id = child.id,
        name = Some(child.name),
        //childcareCost derived from the cost screen - if childcareCost is not asked for the user then make it as zero
        childcareCost = childCareCost,
       //For MVP child cost period is Monthly
        childcareCostPeriod = Periods.Monthly,
        // dob is derived from child details screen.
        dob = child.dob.get,
        disability = disability,
        education = education
      )
    }
  }

  private def createClaimants(claimants : List[_root_.models.claimant.Claimant]) :List[Claimant] = {

    Logger.debug(s"PayloadEligibilityService.createClaimants")

    // if the size is one then
    val claimantList = claimants.size match {
      case 1 => List(createClaimant(claimants.head))
      case _ => List(createClaimant(claimants.head),
        createClaimant(claimants.tail.head, isPartner = true))
    }
    claimantList
  }

  private def createClaimant(claimant : _root_.models.claimant.Claimant, isPartner : Boolean = false): Claimant =  {

    Logger.debug(s"PayloadEligibilityService.createClaimant")
    //claimantDisability maps to the claimant benefits screen
    //*Note : For now consider incapacitated as false for MVP.
    val claimantDisability = Disability(
      disabled = claimant.disability.disabled,
      severelyDisabled = claimant.disability.severelyDisabled,
      incapacitated  = false
    )

    //otherSupport maps to the claimant benefits screen
    val claimantOtherSupport = OtherSupport(
      disabilityBenefitsOrAllowances = claimant.disability.disabled,
      severeDisabilityBenefitsOrAllowances = claimant.disability.severelyDisabled,
      incomeBenefitsOrAllowances = claimant.disability.incomeBenefits,
      carersAllowance = claimant.disability.carersAllowance
    )

    //if previous income is selected No then evaluate it to 0. Else sum up all the incomes to get the totalIncome
    val previousIncome = getPreviousIncomes(claimant.previousIncome)
    //previous total income is summation of other income, benefits, employment income minus pension
    val previousTotalIncome =  getTotalPreviousIncome(previousIncome)

    //total income is summation of other income, benefits, employment income minus pension
    val totalCurrentIncome = getTotalCurrentIncome(claimant.currentIncome, previousIncome, previousTotalIncome)
    val esVouchersAvailable = claimantService.escVouchersAvailable(claimant)
    val outputClaimant = Claimant(
      //default it to true since we do not have the partner journey. The field maps to Do you live with your partner screen.
      liveOrWork = true,
      totalIncome = totalCurrentIncome,
      //currently we are not using earned income in the eligibility micro service so set it to 0
      earnedIncome = BigDecimal(0.00),
      //hoursPerWeek - is maps to how many hours do ou work screen.
      hoursPerWeek = claimant.hours.get.doubleValue(),
      //default it to false since we do not have the partner journey in MVP. The field maps to Do you live with your partner screen.
      isPartner = isPartner,
      disability = claimantDisability,
     //default to false for MVP. That the claimant is not getting any other scheme.
      schemesClaiming = SchemesClaiming(),
      previousTotalIncome = previousTotalIncome,
      employerProvidesESC  = esVouchersAvailable,
     // default the value of vouchers under ClaimantsElements to true for MVP.
      elements = ClaimantsElements(vouchers = esVouchersAvailable),
      otherSupport = claimantOtherSupport

    )
    outputClaimant
  }

  private def getTotalCurrentIncome(currentIncome : Option[Income], previousIncomes:  Tuple4[BigDecimal, BigDecimal, BigDecimal, BigDecimal], previousTotalIncome : BigDecimal) ={

    Logger.debug(s"PayloadEligibilityService.getTotalCurrentIncome")

   currentIncome match {
      case Some(x) =>
        // if user has not entered the value for a particular income then take that particular value from previous income.
        val benefits = x.benefits match {
          case Some(x) => x
          case _ => previousIncomes._1
        }

        val employmentIncome = x.employmentIncome match {
          case Some(x) => x
          case _ => previousIncomes._2
        }

        val otherIncome = x.otherIncome match {
          case Some(x) => x
          case _ => previousIncomes._3
        }

        val pension = x.pension match {
          case Some(x) => x
          case _ => previousIncomes._4
        }
        (((benefits * ApplicationConfig.noOfMonths) + employmentIncome + otherIncome) - (pension * ApplicationConfig.noOfMonths))

      case _ =>
        //if the user as selected is your current income likely to change as No then take previousIncome has your current income.
        previousTotalIncome
    }

  }

  private def getTotalPreviousIncome(previousIncomes : Tuple4[BigDecimal, BigDecimal, BigDecimal, BigDecimal]) = {

    Logger.debug(s"PayloadEligibilityService.getTotalPreviousIncome")

    //pension is asked monthly so need to multiply it by 12
    ((previousIncomes._1 * ApplicationConfig.noOfMonths) + previousIncomes._2 + previousIncomes._3 - (previousIncomes._4 * ApplicationConfig.noOfMonths))
  }

  private def getPreviousIncomes(previousIncome: Option[Income]) = {
    Logger.debug(s"PayloadEligibilityService.getPreviousIncomes")
    previousIncome match {
      case Some(x) => (getIncomeValue(x.benefits), getIncomeValue(x.employmentIncome), getIncomeValue(x.otherIncome),getIncomeValue(x.pension))
      case _ =>   (BigDecimal(0.00), BigDecimal(0.00), BigDecimal(0.00), BigDecimal(0.00))
    }
  }

  private def getIncomeValue(value : Option[BigDecimal]): BigDecimal = {
    Logger.debug(s"PayloadEligibilityService.getIncomeValue")
    value match {
      case Some(x) => x
      case _ => BigDecimal(0.00)
    }

  }

}
