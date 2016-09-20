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

import _root_.config.ApplicationConfig
import controllers.keystore.CCSession
import controllers.manager.{ClaimantManager, HelperManager, ChildrenManager}
import _root_.models.payload.calculator.output.tc.{TCCalculatorOutput, TCTaxYear}
import _root_.models.payload.eligibility.output.tc.TCEligibilityOutput
import _root_.models.pages.results.{EscVouchersAvailablePageModel, Scheme, FreeEntitlementPageModel, ResultsPageModel}
import _root_.models.payload.eligibility.output.esc.ESCEligibilityOutput
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Days, LocalDate}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.{AuditEvents, ResultService}
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future

/**
 * Created by user on 18/03/16.
 */
object ResultsController extends ResultsController with CCSession with KeystoreService with ChildrenManager with HelperManager with ClaimantManager {
  override val resultService = ResultService
  override val auditEvents = AuditEvents
}

trait ResultsController extends FrontendController {
  this: CCSession with KeystoreService with ChildrenManager with HelperManager with ClaimantManager =>

  val resultService : ResultService

  val auditEvents : AuditEvents

  private def loadKeyStore(implicit hc: HeaderCarrier, request: Request[AnyContent]) = {
    Logger.debug(s"ResultsController.loadKeyStore")

    for {
      children <- cacheClient.loadChildren()
      claimant <- cacheClient.loadClaimants()
      household <- cacheClient.loadHousehold()
    } yield (children, claimant, household)
  }

  private def calculateWTCProRataAmount(taxYear : TCTaxYear, proRataEnd : LocalDate) = {
    Logger.debug(s"ResultsController.calculateWTCProRataAmount")

    val periods = taxYear.periods
    val wtcTotalChildCareAmount = periods.foldLeft(BigDecimal(0.00))((periodWTCChildCareAmount, period) => {
      periodWTCChildCareAmount + period.elements.wtcChildcareElement.netAmount
    })
    val noOfDaysInTaxYear = Days.daysBetween(taxYear.from, taxYear.until).getDays

    val noOfEligibleDays = Days.daysBetween(taxYear.from, proRataEnd).getDays

    (wtcTotalChildCareAmount/noOfDaysInTaxYear) * noOfEligibleDays

  }

  private def calculateTCAmounts(tcOutput: TCCalculatorOutput, household : _root_.models.household.Household) = {
    Logger.debug(s"ResultsController.calculateTCAmounts")

    val tcChildCareAmount = tcOutput.taxYears.foldLeft(BigDecimal(0.00))((totalTCAmount, taxYear) => {

      val proRataEnd = tcOutput.proRataEnd.get
      if (proRataEnd.isAfter(taxYear.from) && proRataEnd.isBefore(taxYear.until)) {
        //proRata the wtc child element
        totalTCAmount + calculateWTCProRataAmount(taxYear, taxYear.proRataEnd.get)
      }
      else {
        totalTCAmount + taxYear.periods.foldLeft(BigDecimal(0.00))((periodTCAmount, period) => {
          periodTCAmount + period.elements.wtcChildcareElement.netAmount
        })
      }
    })

    val tcOtherAmount = (tcOutput.totalAwardProRataAmount - tcChildCareAmount)
    (tcChildCareAmount, tcOtherAmount)
  }

  private def getChildCareAnnualCost(children: List[_root_.models.child.Child]) = {
    Logger.debug(s"ResultsController.getChildCareAnnualCost")
    ApplicationConfig.noOfMonths * children.foldLeft(BigDecimal(0.00))((acc, child) => {
      child.childCareCost match {
        case Some(cost) => acc + cost
        case _ => acc+ 0
      }
    })
  }

  private def getTCEligibility(tcEligibilityResult : TCEligibilityOutput) = {
    Logger.debug(s"ResultsController.getTCEligibility")
    tcEligibilityResult.taxYears.filter(x => x.periods.filter(x => (
      if (x.householdElements.basic)
        true
      else
        false)).isEmpty).isEmpty
  }

  private def auditData(auditData : Tuple10[String, String, String, String, String, String, String, String, String, String],
                        claimants : List[_root_.models.claimant.Claimant],
                        children: List[_root_.models.child.Child],
                        resultsPageModel : _root_.models.pages.results.ResultsPageModel)
                       (implicit request: Request[_], hc: HeaderCarrier) = {
    Logger.debug(s"ResultsController.auditData")
    auditEvents.auditResultSummary(auditData,claimants, children)
    auditEvents.auditClaimantChildrenBenefits(claimants, children)
    auditEvents.auditResultPageDetails((Json.toJson[_root_.models.pages.results.ResultsPageModel](resultsPageModel)).toString())
    auditEvents.auditCostPerAge(children)
  }

  private def determineFreeEntitlementMessage(claimant : _root_.models.claimant.Claimant, childrenList: List[_root_.models.child.Child], tfcEligibility : Boolean) : Option[FreeEntitlementPageModel] = {
    val whereDoYouLIve = claimant.whereDoYouLive match {
      case Some(x) => true
      case _ => false
    }
    if (whereDoYouLIve) {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      val firstSept2017 = LocalDate.parse("2017-09-01", formatter)
      val twoYearOld = childrenList.filter(x => age(x.dob) == ApplicationConfig.freeEntitlementTwoYearOld).nonEmpty
      val threeYearOld = childrenList.filter(x => age(x.dob) == ApplicationConfig.freeEntitlementThreeYearOld).nonEmpty
      val fourYearOld = childrenList.filter(x => age(x.dob) == ApplicationConfig.freeEntitlementFourYearOld).nonEmpty
      val threeFourYearOldSep2017 = childrenList.filter(x => age(x.dob, currentDate = firstSept2017) == ApplicationConfig.freeEntitlementThreeYearOld || age(x.dob, currentDate = firstSept2017) == ApplicationConfig.freeEntitlementFourYearOld).nonEmpty
      Some(FreeEntitlementPageModel(
        twoYearOld = twoYearOld,
        threeYearOld = threeYearOld,
        fourYearOld = fourYearOld,
        threeFourYearOldSep2017 = threeFourYearOldSep2017,
        region = claimant.whereDoYouLive.get,
        tfcEligibility = tfcEligibility
      ))
    }
    else {
      None
    }
  }

  private def calculateTCAmountUserInput(household : _root_.models.household.Household)  = {
    household.benefits.get.tcAmount match {
      case Some(x) => x.intValue() * ApplicationConfig.tcPaymentWeeksInAYear
      case _ => 0
    }
  }

  private def calculateUCAmount(household : _root_.models.household.Household) : Int = {
    household.benefits.get.ucAmount match {
      case Some(x) => x.intValue() * ApplicationConfig.ucFrequency
      case _ => 0
    }
  }

  private def determineEscVouchersAvailable(claimants : List[_root_.models.claimant.Claimant]) = {
    EscVouchersAvailablePageModel(
      parent = claimantService.escVouchersAvailable(claimants.head),
      partner = claimants.size match {
        case 2 => Some(claimantService.escVouchersAvailable(claimants.tail.head))
        case _ => None
      }
    )
  }

  def onPageLoad = sessionProvider.withSession {
    Logger.debug(s"ResultsController.onPageLoad")
    implicit request =>

      loadKeyStore.flatMap {
        case (Some(children), Some(claimants), Some(household)) =>
          if(children.isEmpty && claimants.isEmpty) {
            Future.successful(sessionProvider.redirectLoadDifficulties)
          } else {
            resultService.getEligibilityResult(claimants, children).flatMap {
              eligibilityResult =>
                resultService.getCalculatorResult(eligibilityResult,claimants, children).flatMap {
                  calculatorResult =>
                    val annualCost = getChildCareAnnualCost(children)

                    val tfcEligibility = eligibilityResult.eligibility.tfc.get.householdEligibility
                    val tfcAmount =  calculatorResult.calculation.tfc match {
                      case Some(x) =>
                        //if household eligibility is false and even if we get a response from calculator - show the amount as zero
                        tfcEligibility match {
                          case true => x.householdContribution.government
                          case _ =>  BigDecimal(0.00)
                        }
                      case _ =>  BigDecimal(0.00)
                    }

                    //if user has input the value use those
                    val tcEligibility = getTCEligibility(eligibilityResult.eligibility.tc.get)
                    val tcAmount  =  if (!household.benefits.get.tcAmount.isDefined) {
                      calculatorResult.calculation.tc match {
                        //if basic element is false and even if we get a response from calculator - show the amount as zero
                        case Some(x) =>

                          tcEligibility match {
                            case true => calculateTCAmounts(x, household)
                            case _ => (BigDecimal(0.00), BigDecimal(0.00))
                          }
                        case _ => (BigDecimal(0.00), BigDecimal(0.00))
                      }
                    }
                    else {
                      (BigDecimal(0.00), BigDecimal(0.00))
                    }

                    val escAmount =  calculatorResult.calculation.esc match {
                      case Some(x) => x.totalSavings.totalSaving
                      case _ => BigDecimal(0.00)
                    }

                    val tcAmountByUser = calculateTCAmountUserInput(household)
                    val ucAmountByUser = calculateUCAmount(household)

                    val tcAmounts = if(tcAmountByUser > 0)
                      tcAmountByUser
                    else if(ucAmountByUser > 0)
                      ucAmountByUser
                    else ((tcAmount._1.toInt)+(tcAmount._2.toInt))

                    def sortedScheme = Seq(Scheme("TFC", tfcAmount.toInt), Scheme("ESC", escAmount.toInt), Scheme("TC", tcAmounts)).sortWith(_.amount > _.amount)
                    val escClaimantEligibilityResult = claimantService.getEscEligibility(eligibilityResult.eligibility.esc.get)
                    val escChildrenEligibilityResult = childrenService.getEscEligibility(eligibilityResult.eligibility.esc.get)
                    val resultsPageModel = ResultsPageModel(
                      annualCost = annualCost.toInt,
                      sortedScheme,
                      tcAmountByUser = tcAmountByUser,
                      ucAmountByUser = ucAmountByUser,
                      tfcEligibility = tfcEligibility,
                      tcEligibility = tcEligibility,
                      escEligibility = if((escClaimantEligibilityResult._1 || escClaimantEligibilityResult._2) && escChildrenEligibilityResult) true else false,
                      freeEntitlement = determineFreeEntitlementMessage(claimants.head, children, tfcEligibility),
                      escVouchersAvailable = determineEscVouchersAvailable(claimants)
                    )

                    val auditDataMap = (tfcAmount.toString(), tcAmounts.toString(), escAmount.toString(), tcAmountByUser.toString(), ucAmountByUser.toString(), tfcEligibility.toString(), tcEligibility.toString(), escClaimantEligibilityResult._1.toString, escClaimantEligibilityResult._2.toString, annualCost.toString())

                    auditData(auditDataMap,claimants, children,resultsPageModel)

                    Future.successful( Ok(views.html.results(resultsPageModel)))
                }recover {
                  case e : Exception =>
                    Logger.warn(s"ResultsController exception occurred while invoking calculator microservice: ${e.getMessage}")
                    sessionProvider.redirectLoadDifficulties
                }

            }recover {
              case e : Exception =>
                Logger.warn(s"ResultsController exception occurred while invoking eligibility microservice: ${e.getMessage}")
                sessionProvider.redirectLoadDifficulties
            }
          }
        case _ =>
          Future.successful(sessionProvider.redirectLoadDifficulties)
      } recover {
        case e: Exception =>
          Logger.warn(s"ResultsController onPageLoad exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }
}
