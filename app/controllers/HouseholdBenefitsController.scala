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

import controllers.keystore.CCSession
import controllers.manager.{HelperManager, FormManager}
import form.HouseholdBenefitsForm
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Call, AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object HouseholdBenefitsController extends HouseholdBenefitsController with CCSession with KeystoreService with FormManager with HelperManager{
  override val auditEvent = AuditEvents
}

trait HouseholdBenefitsController extends FrontendController  {

  this: CCSession with KeystoreService  with FormManager with HelperManager =>

  val auditEvent : AuditEvents

  private def getClaimants (implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    cacheClient.loadClaimants().map {
      case Some(x) =>
        x
      case None =>
        List()
    }
  }

  private def constructBackUrl(claimant : List[ _root_.models.claimant.Claimant])(implicit hc : HeaderCarrier, request : Request[AnyContent]) : Call = {
    val escVouchersAvailable = if(claimant.size > 1) claimant.tail.head.escVouchersAvailable else None

    (claimant.size, escVouchersAvailable) match {
      case (1, _) => routes.DoYouLiveWithPartnerController.onPageLoad()
      case (2, Some(x)) =>routes.ESCVouchersController.onPageLoadPartner()
      case (_, _) => routes.ClaimantHoursController.onPageLoadPartner()
    }
  }

  private def saveHousehold(household : _root_.models.household.Household)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    cacheClient.saveHousehold(household).map {
      result =>
        auditEvent.auditHouseholdBenefits((Json.toJson[_root_.models.household.Household](household)).toString())
        Redirect(routes.ResultsController.onPageLoad())
    } recover{
      case e : Exception =>
        Logger.warn(s"HouseholdBenefitsController loadHousehold exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  //error.real gets thrown in 2 situtations one when you enter non digits(i.e special characters, alphabets) in the input field
  // or when we leave the input field empty(since in the form we using mandatory option for validation)
  // if the selection is made and the input feild is left empty we will replace error.real with correct error message
  private def getCorrectErrorMessage(form: Form[_root_.models.pages.HouseholdBenefitsPageModel], key: String) : String = {

    if(form.errors.filter(x => x.message == key).nonEmpty) {
      val data = form.data
      if(data.get("benefits.tcBenefitSelection").isDefined && (data.get("benefits.tcBenefitAmount")).get.isEmpty)
        "cc.household.benefits.tc.amount.empty"
      else if(data.get("benefits.ucBenefitSelection").isDefined && (data.get("benefits.ucBenefitAmount")).get.isEmpty)
        "cc.household.benefits.uc.amount.empty"
      else
        "cc.household.benefits.error.not.a.number"
    }
    else
      ""
  }

  private def saveHouseholdBenefits(pageModel: _root_.models.pages.HouseholdBenefitsPageModel)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    val newTCAmount = pageModel.benefits.tcBenefitAmount
    val newUCAmount = pageModel.benefits.ucBenefitAmount

    cacheClient.loadHousehold().flatMap {
      case Some(household) =>
        val modifiedHouseholdBenefits =
         if(pageModel.benefits.tcBenefitSelection)
           household.copy(Some(_root_.models.household.Benefits(tcAmount = newTCAmount, ucAmount = None)))
         else  if(pageModel.benefits.ucBenefitSelection)
           household.copy(Some(_root_.models.household.Benefits(tcAmount = None, ucAmount = newUCAmount)))
        else
           household.copy(Some(_root_.models.household.Benefits(tcAmount = None, ucAmount = None)))

        saveHousehold(modifiedHouseholdBenefits)

      case None =>
        val household =  if(pageModel.benefits.tcBenefitSelection)
          _root_.models.household.Household(Some(_root_.models.household.Benefits(tcAmount = newTCAmount, ucAmount = None)))
        else  if(pageModel.benefits.ucBenefitSelection)
          _root_.models.household.Household(Some(_root_.models.household.Benefits(tcAmount = None, ucAmount = newUCAmount)))
        else
          _root_.models.household.Household(Some(_root_.models.household.Benefits(tcAmount = None, ucAmount = None)))

        saveHousehold(household)
    }recover{
      case e : Exception =>
        Logger.warn(s"HouseholdBenefitsController loadHousehold exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }


  private def loadBenefits(household : Option[_root_.models.household.Household]) (implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    getClaimants.map {
      x =>
        if (x.isEmpty) {
          Redirect(routes.ClaimantBenefitsController.onPageLoadParent())
        }
        else {
          household match {
            case Some(l) =>
              l.benefits match {
                case Some(benefit) =>

                  val benefitsPageModel = _root_.models.pages.HouseholdBenefitsPageModel(_root_.models.pages.BenefitsPageModel(
                    tcBenefitSelection = if(benefit.tcAmount.isDefined) true else false,
                    tcBenefitAmount = benefit.tcAmount,
                    ucBenefitSelection = if(benefit.ucAmount.isDefined) true else false,
                    ucBenefitAmount = benefit.ucAmount,
                    noBenefitSelection = if(benefit.tcAmount.isDefined || benefit.ucAmount.isDefined) false else true
                  ))

                  Ok(views.html.householdBenefits(HouseholdBenefitsForm.form.fill(benefitsPageModel), backUrl = constructBackUrl(x)))
                case None =>
                  Ok(views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl = constructBackUrl(x)))
              }
            case None =>
              Ok(views.html.householdBenefits(HouseholdBenefitsForm.form, backUrl = constructBackUrl(x)))
          }
        }
    }
  }

  def onPageLoad  = sessionProvider.withSession {
    implicit request =>
      cacheClient.loadHousehold().flatMap {
        case x => loadBenefits(x)
      } recover {
        case e: Exception =>
          Logger.warn(s"HouseholdBenefitsController loadHousehold exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }


  def onSubmit = sessionProvider.withSession {
    implicit request =>

      HouseholdBenefitsForm.form.bindFromRequest().fold(
        errors => {
          getClaimants.map {
            x =>

              val modified = formService.overrideFormError[_root_.models.pages.HouseholdBenefitsPageModel](form = errors, key = "error.real", value = getCorrectErrorMessage(errors, "error.real"))
              BadRequest(views.html.householdBenefits(modified, backUrl = constructBackUrl(x)))
          }
        },
        success => {

          saveHouseholdBenefits(success)
        }
      )
  }

}
