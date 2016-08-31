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
import controllers.manager.{HelperManager, ChildrenManager, ClaimantManager, FormManager}
import form.ClaimantLocationForm
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Call, AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future


object ClaimantLocationController extends ClaimantLocationController with CCSession with KeystoreService with ClaimantManager with FormManager with ChildrenManager with HelperManager {
  override val auditEvent = AuditEvents
}

trait ClaimantLocationController extends FrontendController {


  this: CCSession with KeystoreService with ClaimantManager with FormManager   =>

  val auditEvent : AuditEvents

  def onPageLoad = sessionProvider.withSession {
    implicit request =>
      cacheClient.loadClaimants.map {
        case Some(claimants) =>
          claimants.head.whereDoYouLive match {
 
            case Some(i) => Ok(views.html.claimantLocation(ClaimantLocationForm.form.fill(Some(i)), backUrl = constructBackUrl(claimants.head)))
            case _ => Ok(views.html.claimantLocation(ClaimantLocationForm.form, backUrl = constructBackUrl(claimants.head)))
          }
        case None => //redirect to benefits controller
          Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
      } recover {
        case e : Exception =>
          Logger.warn(s"Claimant location controller onPageLoad exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  private def constructBackUrl(claimant : _root_.models.claimant.Claimant)(implicit hc : HeaderCarrier, request : Request[AnyContent]) : Call = {
    if (claimant.escVouchersAvailable.isDefined)
      routes.ESCVouchersController.onPageLoadParent()
    else
      routes.ClaimantHoursController.onPageLoadParent()
  }

  def onSubmit = sessionProvider.withSession {
    implicit request =>
      ClaimantLocationForm.form.bindFromRequest().fold(
        errors => {
          cacheClient.loadClaimants.map {
            case Some(claimants) =>
              Logger.warn(s"claimant location error type: ${errors.errors}")
              BadRequest(views.html.claimantLocation(errors, backUrl = constructBackUrl(claimants.head)))
            case None => //redirect to benefits controller
              Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
          } recover {
            case e : Exception =>
             // Logger.warn(s"Claimant location controller onPageLoad exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }
        },
        success =>
          cacheClient.loadClaimants.flatMap {
            case Some(x) =>
              val claimant = x.head
              val modifiedClaimant = claimant.copy(whereDoYouLive = success)
              val modifiedList = claimantService.replaceClaimantInAList(x, modifiedClaimant, 1)
              auditEvent.auditClaimantLocation((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
              cacheClient.saveClaimants(modifiedList).map {
                result =>
                  Redirect(controllers.routes.DoYouLiveWithPartnerController.onPageLoad())
              }
            case _ => //redirect to benefits controller
              Future.successful(Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent()))
          } recover {
            case e : Exception =>
              sessionProvider.redirectLoadDifficulties
          }
      )
  }
}
