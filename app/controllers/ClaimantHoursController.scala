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
import controllers.manager.{ChildrenManager, ClaimantManager, FormManager, HelperManager}
import form.ClaimantHoursFormInstance
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


object ClaimantHoursController extends ClaimantHoursController with CCSession with KeystoreService with ClaimantManager with FormManager with ChildrenManager with HelperManager {
  override val auditEvent = AuditEvents
}

trait ClaimantHoursController extends FrontendController {

  this: CCSession with KeystoreService with ClaimantManager with FormManager  with HelperManager with ChildrenManager =>

  val auditEvent : AuditEvents

  private def loadClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    cacheClient.loadClaimants.map {
      case Some(x) =>
        val claimant = index match {
          case i if i == 1 => x.head
          case _ => x.tail.head
        }
        val pageModel = populatePageModel(x, index)
        Ok(views.html.claimantHours(new ClaimantHoursFormInstance(parent = if (index == 1) true else false, claimant.previousIncome, claimant.currentIncome).form.fill(pageModel), index))
      case None =>
        Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantHoursController.loadClaimant exception: ${e.getMessage} trace: ${e.printStackTrace()}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def populatePageModel(claimants : List[_root_.models.claimant.Claimant], index: Short) = {
    val claimant = claimantService.getClaimantById(claimants, index)
    _root_.models.pages.ClaimantHoursPageModel(
      numberOfHours = claimant.hours
    )
  }

  private def saveClaimant(index: Short) (implicit hc : HeaderCarrier, request : Request[AnyContent]) = {

    Logger.debug(s"ClaimantHoursController.saveClaimant")
    cacheClient.loadClaimants().flatMap {
      case Some(claimants) =>
        val claimant =  claimantService.getClaimantById(claimants, index)
        new ClaimantHoursFormInstance(parent = if (index == 1) true else false, claimant.previousIncome, claimant.currentIncome).form.bindFromRequest().fold(
          errors => {
            val modified = formService.overrideFormError[_root_.models.pages.ClaimantHoursPageModel](form = errors, key = "error.real", value = "cc.claimant.hours.error.not.a.number")
            Future.successful(
              BadRequest(views.html.claimantHours(modified, index))
            )
          },
          success =>
            saveClaimantHours(claimants, index, success)
        )
      case None =>
        Future.successful(sessionProvider.redirectLoadDifficulties)
    }recover {
      case e: Exception =>
        Logger.warn(s"ClaimantHoursController.onPageLoad exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def saveClaimantHours(claimants : List[_root_.models.claimant.Claimant], index: Short, pageModel : _root_.models.pages.ClaimantHoursPageModel) (implicit hc : HeaderCarrier, request : Request[AnyContent]) = {

    cacheClient.loadChildren().flatMap {
      case Some(children) =>
        val claimant = claimantService.getClaimantById(claimants, index)
        val newHours = pageModel.numberOfHours
        val freeEntitlement = childrenService.childBenefitsEligibility(children)
        val claimantHoursGreaterThanZero = if(newHours.get > 0) true else false
        //reset where do you live and escVouchers available to prevent incorrect pages being displayed on re-entry of different data
        val whereDoYouLive = if(!freeEntitlement) None else claimant.whereDoYouLive
        val escVouchers = if(!claimantHoursGreaterThanZero) None else claimant.escVouchersAvailable

        val modifiedClaimant = claimant.copy(hours = newHours, whereDoYouLive = whereDoYouLive, escVouchersAvailable = escVouchers)
        val modifiedList = claimantService.replaceClaimantInAList(claimants, modifiedClaimant, index)
        cacheClient.saveClaimants(modifiedList).flatMap{
          result =>

                auditEvent.auditClaimantHoursData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())

                (index, claimantHoursGreaterThanZero, freeEntitlement) match {
                  case (1, true, _) => Future.successful(Redirect(controllers.routes.ESCVouchersController.onPageLoadParent()))
                  case (1, _ , true)  => Future.successful(Redirect(controllers.routes.ClaimantLocationController.onPageLoad()))
                  case (1, _ , _) => Future.successful(Redirect(controllers.routes.DoYouLiveWithPartnerController.onPageLoad()))
                  case (2, true, _) => Future.successful(Redirect(controllers.routes.ESCVouchersController.onPageLoadPartner()))
                  case (_, _, _)  => Future.successful(Redirect(controllers.routes.HouseholdBenefitsController.onPageLoad()))
              }

          } recover {
            case e : Exception =>
              sessionProvider.redirectLoadDifficulties
          }
      case _ => //redirect to how many children controller
        Future.successful(Redirect(controllers.routes.HowManyChildrenController.onPageLoad()))
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantHoursController.saveClaimantHours exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onPageLoadParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantHoursController.onPageLoadParent")
    implicit request =>
      loadClaimant(1)
  }

  def onSubmitParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantHoursController.onSubmitParent")
    implicit request =>
      saveClaimant(1)
  }

  def onPageLoadPartner = sessionProvider.withSession {
    implicit request =>
      loadClaimant(2)
  }

  def onSubmitPartner = sessionProvider.withSession {
    implicit request =>
      saveClaimant(2)
  }
}
