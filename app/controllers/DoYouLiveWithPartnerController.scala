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
import controllers.manager.{ FormManager, ClaimantManager}
import form.DoYouLiveWithPartnerForm
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Call, AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by elsie on 11/04/16.
 */
object DoYouLiveWithPartnerController extends DoYouLiveWithPartnerController with CCSession with KeystoreService with ClaimantManager with FormManager{
  override val auditEvent = AuditEvents
}

trait DoYouLiveWithPartnerController extends FrontendController {

  this: CCSession with KeystoreService with ClaimantManager with FormManager   =>

  val auditEvent : AuditEvents

  def onPageLoad = sessionProvider.withSession {
    implicit request =>
      cacheClient.loadClaimants().map {
        case Some(claimant) =>
          val doYouLiveWithPartner = claimant.head.doYouLiveWithPartner

          Ok(views.html.doYouLiveWithPartner(DoYouLiveWithPartnerForm.form.fill(doYouLiveWithPartner), backUrl = constructBackUrl(claimant.head)))
        case None =>
          Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
      } recover {
        case e : Exception =>
          Logger.warn(s"DoYouLiveWithPartnerController onPageLoad exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  private def constructBackUrl(claimant : _root_.models.claimant.Claimant)(implicit hc : HeaderCarrier, request : Request[AnyContent]) : Call = {
    (claimant.whereDoYouLive, claimant.escVouchersAvailable) match {
      case (Some(x), _) => routes.ClaimantLocationController.onPageLoad()
      case (_, Some(x)) => routes.ESCVouchersController.onPageLoadParent()
      case (_, _) => routes.ClaimantHoursController.onPageLoadParent()
    }
  }

  private def saveClamaints(success : Option[Boolean])(implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    cacheClient.loadClaimants().flatMap {
      case Some(claimantList) =>
        val claimant = claimantList.head
        val doYouLiveWithPartner = success.get
        // if the user live's with partner then add the partner in the claimant list else drop
        val modifiedClaimantList = doYouLiveWithPartner match {

          case true =>  if (claimantList.size == 1) {
            claimantService.modifyListOfClaimants(claimantList, 2)}
          else
            claimantList

          case _ =>   if (claimantList.size == 2)
            claimantService.dropClaimantAtIndex(claimantList, 2)
          else
            claimantList
        }
        val modifiedClaimant = claimant.copy(doYouLiveWithPartner = success)
        val modifiedList = claimantService.replaceClaimantInAList(modifiedClaimantList,modifiedClaimant,1)
        cacheClient.saveClaimants(modifiedList).flatMap {
          result =>
            //if you live with your partner then capture the partner details else go to result page
            auditEvent.auditDoYouLiveWithPartnerData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
            if (doYouLiveWithPartner) {
              Future.successful(Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadPartner()))
            }
            else {
              Future.successful(Redirect(controllers.routes.HouseholdBenefitsController.onPageLoad()))
            }

        } recover {
          case e : Exception =>
            sessionProvider.redirectLoadDifficulties
        }

      case None =>
        Future.successful(Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent()))
    } recover {
      case e : Exception =>
        Logger.warn(s"DoYouLiveWithPartnerController onSubmit exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onSubmit = sessionProvider.withSession {
    implicit request =>
      DoYouLiveWithPartnerForm.form.bindFromRequest().fold(
        errors => {
          cacheClient.loadClaimants().map {
            case Some(claimant) =>

              BadRequest(views.html.doYouLiveWithPartner(errors , backUrl = constructBackUrl(claimant.head)))

          case None =>
            Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
        } recover {
          case e : Exception =>
            Logger.warn(s"DoYouLiveWithPartnerController onPageLoad exception: ${e.getMessage}")
            sessionProvider.redirectLoadDifficulties
        }
        },
        success =>
          saveClamaints(success)
      )
  }
}
