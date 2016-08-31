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
import form.ClaimantBenefitsFormInstance
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

/**
 * Created by user on 19/02/16.
 */
object ClaimantBenefitsController extends ClaimantBenefitsController with CCSession with KeystoreService with ClaimantManager with FormManager with ChildrenManager with HelperManager {
  override val auditEvent = AuditEvents
}

trait ClaimantBenefitsController extends FrontendController {

  this: CCSession with KeystoreService with ClaimantManager with FormManager with ChildrenManager with HelperManager =>

  val auditEvent : AuditEvents

  private def constructBackUrl(childrenList : List[_root_.models.child.Child], claimantIndex: Short) = {
    val index = childrenList.size
    val child = childrenService.getChildById(index, childrenList)
    val dob = child.dob
    val today = LocalDate.now()

    val september1stForBirthday = september1stFollowingChildBirthday(dob.get)
    val childAge1stSept = age(Some(september1stForBirthday))
    val childAgeToday = age(dob)

    if (claimantIndex == 1) {
      if (childrenService.childIsLessThan20YearsOld(childAgeToday)) {
        if (childrenService.childIsAgedBetween15And16(childAge1stSept) && child.disability.nonDisabled) {
          routes.ChildDetailsController.onPageLoad(index)
        }
        else {
          routes.ChildCareCostController.onPageLoad(index)
        }
      }
      else {
        routes.ChildDetailsController.onPageLoad(index)
      }
    }
    else {
      routes.DoYouLiveWithPartnerController.onPageLoad()
    }
  }


  private def populateParentBenefitsModel(claimant : _root_.models.claimant.Claimant): _root_.models.pages.ClaimantBenefitsPageModel = {
    val  parentBenefitsPageModel = _root_.models.pages.ClaimantBenefitsPageModel(
      disabilityBenefit = claimant.disability.disabled,
      severeDisabilityBenefit = claimant.disability.severelyDisabled,
      incomeBenefit = claimant.disability.incomeBenefits,
      carerAllowanceBenefit = claimant.disability.carersAllowance,
      noBenefit = claimant.disability.noBenefits
    )
    parentBenefitsPageModel
  }

  private def saveClaimants(claimants : List[_root_.models.claimant.Claimant],pageModel : _root_.models.pages.ClaimantBenefitsPageModel, index : Short)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    val newClaimantDisability = claimantService.copyPageModelToClaimantDisability(pageModel, index)
    val claimant = claimantService.getClaimantById(claimants, index)
    val modifiedClaimant = claimant.copy(disability = newClaimantDisability)
    val modifiedList = claimantService.replaceClaimantInAList(claimants, modifiedClaimant, index)
    cacheClient.saveClaimants(modifiedList).map {
      result =>
        if (index == 1) {
          auditEvent.auditClaimantBenefitsData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
          Redirect(controllers.routes.ClaimantIncomeLastYearController.onPageLoadParent())
        } else {
          auditEvent.auditPartnerBenefitsData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
          Redirect(controllers.routes.ClaimantIncomeLastYearController.onPageLoadPartner())
        }
    } recover {
      case e : Exception =>
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def saveClaimantBenefits(pageModel : _root_.models.pages.ClaimantBenefitsPageModel, index : Short)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    cacheClient.loadClaimants().flatMap {
      case Some(l) =>
        saveClaimants(l, pageModel, index)
      case None =>
        val claimantList = claimantService.createListOfClaimants(index)
        saveClaimants(claimantList, pageModel, index)
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantBenefitsController saveClaimantBenefits exception : ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def getChildrenList(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    cacheClient.loadChildren().map {
      case Some(x) =>
        x
      case None =>
        List()
    }
  }

  private def loadParent(claimants: Option[List[_root_.models.claimant.Claimant]], index : Short)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    getChildrenList.map {
      x =>
        if (x.isEmpty){
          Redirect(routes.HowManyChildrenController.onPageLoad())
        }
        else  {
          val backUrl  = constructBackUrl(x, index)
          claimants match {
            case Some(l) =>
              val parentBenefitsModel = populateParentBenefitsModel(claimantService.getClaimantById(l, index))
              Ok(views.html.claimantBenefits((new ClaimantBenefitsFormInstance).form.fill(parentBenefitsModel), index, backUrl))
            case None =>
              Ok(views.html.claimantBenefits((new ClaimantBenefitsFormInstance).form, index, backUrl))
          }
        }
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantBenefitsController loadParent exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }


  def onPageLoadParent = sessionProvider.withSession {
    implicit request =>
      cacheClient.loadClaimants().flatMap {
        case x => loadParent(x, 1)
      } recover {
        case e : Exception =>
          Logger.warn(s"ClaimantBenefitsController onPageLoadParent exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  def onSubmitParent = sessionProvider.withSession {
    implicit request =>
      new ClaimantBenefitsFormInstance(parent = true).form.bindFromRequest().fold(
        errors => {
          getChildrenList.map {
            x =>
              BadRequest(views.html.claimantBenefits(errors, 1, constructBackUrl(x, 1)))
          } recover {
            case e : Exception =>
              Logger.warn(s"ClaimantBenefitsController onSubmitParent exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }
        },
        success =>
          saveClaimantBenefits(success, 1)
      )
  }

  def onPageLoadPartner = sessionProvider.withSession {
    implicit request =>
      cacheClient.loadClaimants().flatMap {
        case x => loadParent(x, 2)
      } recover {
        case e : Exception =>
          Logger.warn(s"ClaimantBenefitsController onPageLoadPartner exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  def onSubmitPartner = sessionProvider.withSession {
    implicit request =>
      new ClaimantBenefitsFormInstance(parent = false).form.bindFromRequest().fold(
        errors => {
          getChildrenList.map {
            x =>
              BadRequest(views.html.claimantBenefits(errors, 2, constructBackUrl(x, 2)))
          } recover {
            case e : Exception =>
              Logger.warn(s"ClaimantBenefitsController onSubmitPartner exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }
        },
        success =>
          saveClaimantBenefits(success, 2)
      )
  }

}
