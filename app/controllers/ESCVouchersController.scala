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
import form.ESCVouchersFormInstance
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request, Result}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by user on 11/07/16.
 */

object ESCVouchersController extends ESCVouchersController with CCSession with KeystoreService with ClaimantManager with FormManager with HelperManager with ChildrenManager {
  override val auditEvent = AuditEvents
}

trait ESCVouchersController extends FrontendController  {

  this: CCSession with KeystoreService with ClaimantManager with FormManager  with HelperManager  with ChildrenManager =>

  val auditEvent : AuditEvents

  private def loadClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    cacheClient.loadClaimants.map {
      case Some(claimantList) =>
        val claimant = claimantService.getClaimantById(claimantList, index.toInt)

        claimant.escVouchersAvailable match {
          case Some(x) => Ok(views.html.ESCVouchers((new ESCVouchersFormInstance(parent = if(index == 1) true else false)).form.fill(Some(x)), index))
          case _ => Ok(views.html.ESCVouchers((new ESCVouchersFormInstance(parent = if(index == 1) true else false)).form, index))
        }

      case None =>
        Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
    } recover {
      case e : Exception =>
        Logger.warn(s"ESCVouchersController.loadClaimant exception: ${e.getMessage} trace: ${e.printStackTrace()}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def saveClaimants(index: Short, escVouchers : Option[String]) (implicit hc : HeaderCarrier, request : Request[AnyContent]) = {

    cacheClient.loadClaimants().flatMap {
      case Some(claimants) => {
        val claimant = claimantService.getClaimantById(claimants, index)
        val modifiedClaimant = claimant.copy(escVouchersAvailable = escVouchers)
        val modifiedList = claimantService.replaceClaimantInAList(claimants, modifiedClaimant, index)

        cacheClient.saveClaimants(modifiedList).flatMap {
          result =>
            cacheClient.loadChildren().flatMap {
              case Some(children) =>
                if(index == 1) auditEvent.auditClaimantEscVouchersAvailable((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
                else auditEvent.auditPartnerEscVouchersAvailable((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())

                val freeEntitlement = childrenService.childBenefitsEligibility(children)
                (index.toInt, freeEntitlement) match {
                  case (1, true) => Future.successful(Redirect(controllers.routes.ClaimantLocationController.onPageLoad()))
                  case (1, false) => Future.successful(Redirect(controllers.routes.DoYouLiveWithPartnerController.onPageLoad()))
                  case (_, _) => Future.successful(Redirect(controllers.routes.HouseholdBenefitsController.onPageLoad()))
                }
              case _ =>
                Future.successful(sessionProvider.redirectLoadDifficulties)
            } recover {
              case e: Exception =>
                sessionProvider.redirectLoadDifficulties
            }
        } recover {
          case e: Exception =>
            Logger.warn(s"ESCVouchersController.saveClaimantHours exception: ${e.getMessage}")
            sessionProvider.redirectLoadDifficulties
        }
      }
      case _ =>   Future.successful(sessionProvider.redirectLoadDifficulties)
    } recover {
      case e: Exception =>
      Logger.warn(s"ESCVouchersController.saveClaimantHours exception: ${e.getMessage}")
      sessionProvider.redirectLoadDifficulties
    }
  }

  private def saveClaimants(index : Short )  (implicit hc : HeaderCarrier, request : Request[AnyContent]) :Future[Result]  = {
    new ESCVouchersFormInstance().form.bindFromRequest().fold(
      errors => {
        Future.successful(BadRequest(views.html.ESCVouchers(errors, index)))
      },
      success =>
        saveClaimants(index, success)
    )
  }

  def onPageLoadParent = sessionProvider.withSession {
    Logger.debug(s"ESCVouchersController.onPageLoadParent")
    implicit request =>
      loadClaimant(1)
  }

  def onSubmitParent = sessionProvider.withSession {
    Logger.debug(s"ESCVouchersController.onSubmitParent")
    implicit request =>
      saveClaimants(1)
  }

  def onPageLoadPartner = sessionProvider.withSession {
    Logger.debug(s"ESCVouchersController.onPageLoadParent")
    implicit request =>
      loadClaimant(2)
  }

  def onSubmitPartner = sessionProvider.withSession {
    Logger.debug(s"ESCVouchersController.onSubmitPartner")
    implicit request =>
      saveClaimants(2)
  }

}
