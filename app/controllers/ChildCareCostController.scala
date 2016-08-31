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
import controllers.manager.{ChildrenManager, FormManager, HelperManager}
import form.{ChildCareCostKeys, ChildCareCostForm}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by user on 04/02/16.
 */

object ChildCareCostController extends ChildCareCostController with CCSession with KeystoreService with ChildrenManager with HelperManager with FormManager with ChildCareCostKeys {
  override val auditEvent = AuditEvents
}

trait ChildCareCostController extends FrontendController {
  this: CCSession with KeystoreService with ChildrenManager with HelperManager with FormManager with ChildCareCostKeys =>

  val auditEvent : AuditEvents

  private def redirect(children : List[_root_.models.child.Child], index: Int) = {
    Logger.debug(s"ChildCareCostController.redirect")

    if (childrenService.hasRemainingChildren(children = children, currentIndex = index)) {
      Redirect(routes.ChildDetailsController.onPageLoad(index + 1))
    } else {
      Redirect(routes.ClaimantBenefitsController.onPageLoadParent())
    }
  }

  private def getModifiedChild(childCarePageModel: _root_.models.pages.ChildCarePageModel, child: _root_.models.child.Child, showCost : Boolean) = {
    Logger.debug(s"ChildCareCostController.redirect")
    if (showCost){
      //no education show only cost
      child.copy(childCareCost = childCarePageModel.childCareCost, education = None)
    }
    else {
      val education = childCarePageModel.childEducation match {
        case Some(true) =>
          _root_.models.child.Education(inEducation = true, startDate = Some(LocalDate.now()))
        case _ =>
          _root_.models.child.Education(inEducation = false, startDate = None)
      }
      //cost none show only education
      child.copy(childCareCost = None, education = Some(education))
    }
  }

  private def saveChildren(updateList: List[_root_.models.child.Child], index : Int ) (implicit hc: HeaderCarrier, request: Request[AnyContent])   =  {
    Logger.debug(s"ChildCareCostController.saveChildren")
    cacheClient.saveChildren(updateList).map {
      result => {
        redirect(children = updateList, index = index)
      }
    } recover {
      case e : Exception => {
        Logger.warn(s"ChildCareCostController.saveChildren error : ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
      }
    }
  }

  private def saveChild(childCarePageModel : _root_.models.pages.ChildCarePageModel, index: Int)(implicit hc: HeaderCarrier, request: Request[AnyContent])  = {
    Logger.debug(s"ChildCareCostController.saveChild")
    cacheClient.loadChildren().flatMap {
      case Some(children) =>
        val showCost = getShowCost(children, index)
        val child = childrenService.getChildById(index, children)
        val modifiedChild  = getModifiedChild(childCarePageModel, child, showCost)
        val updateList = childrenService.replaceChildInAList(children, index, modifiedChild)
        auditEvent.auditCostAndEducationsData((Json.toJson[_root_.models.child.Child](modifiedChild)).toString())
        saveChildren(updateList, index)
      case None =>
        Logger.debug(s"Redirect to How Many Children page when no session")
        Future.successful(Redirect(routes.HowManyChildrenController.onPageLoad()))
        
    } recover {
      case e : Exception =>
        Logger.warn(s"ChildCareCostController.saveChild error while saving to keyStore: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def populateChildCareModel(child: _root_.models.child.Child, showCost: Boolean): _root_.models.pages.ChildCarePageModel = {
    Logger.debug(s"ChildCareCostController.populateChildCareModel")
   if(showCost) {
     _root_.models.pages.ChildCarePageModel(
       childCareCost = child.childCareCost,
       childEducation = None
     )
   }
    else {
     val inEducation = child.education match {
       case Some(x) => Some(x.inEducation)
       case _ => None
     }

     _root_.models.pages.ChildCarePageModel(
       childCareCost = None,
       childEducation = inEducation
     )
   }
  }

   private def resetCostAndEducationDetails(child : _root_.models.child.Child,children : List[_root_.models.child.Child], index: Int)(implicit hc: HeaderCarrier, request: Request[AnyContent]) = {
     Logger.debug(s"ChildCareCostController.resetCostAndEducationDetails")
     val modifiedChild = child.copy(childCareCost = None, education = None)
     val updateList = childrenService.replaceChildInAList(children, index, modifiedChild)
     cacheClient.saveChildren(updateList).map {
       result =>
         redirect(children = updateList, index = index)
     } recover {
       case e : Exception =>
         Logger.warn(s"ChildCareCostController.resetCostAndEducationDetails error : ${e.getMessage}")
         sessionProvider.redirectLoadDifficulties
     }
   }

  private def getShowCost(childrenList: List[_root_.models.child.Child], index : Int) : Boolean = {
    Logger.debug(s"ChildCareCostController.getShowCost")
    val child = childrenService.getChildById(index, childrenList)
    val dob = child.dob
    val september1stForBirthday = september1stFollowingChildBirthday(dob.get)
    val childAge1stSept = age(Some(september1stForBirthday))

    if (childrenService.childIsLessThan15Or16WhenDisabled(childAge1stSept, !child.disability.nonDisabled)) {
      true
    } else {
      false
    }
  }

  private def redirectPage(children : List[_root_.models.child.Child], index: Int)(implicit hc: HeaderCarrier, request: Request[AnyContent]) = {
    Logger.debug(s"ChildCareCostController.redirectPage")
    val child = childrenService.getChildById(index, children)
    val dob = child.dob
    val september1stForBirthday = september1stFollowingChildBirthday(dob.get)
    val childAge1stSept = age(Some(september1stForBirthday))
    val childAgeToday = age(dob)
    val isChildDisabled = child.disability.blind || child.disability.severelyDisabled || child.disability.disabled

    if (childrenService.childIsLessThan20YearsOld(childAgeToday)) {
      if (childrenService.childIsLessThan15Or16WhenDisabled(childAge1stSept, isChildDisabled)) {
        Future.successful(Ok(views.html.childCareCost(ChildCareCostForm.form.fill(populateChildCareModel(child, showCost = true)), showCost = true, index)))
      } else if (childrenService.childIsAgedBetween15And16(childAge1stSept)) {
        resetCostAndEducationDetails(child, children, index)
      } else {
        Future.successful(Ok(views.html.childCareCost(ChildCareCostForm.form.fill(populateChildCareModel(child, showCost = false)), showCost = false, index)))
      }
    } else {
      resetCostAndEducationDetails(child, children, index)
    }
  }

  def onPageLoad(index: Int) = sessionProvider.withSession {
    Logger.debug(s"ChildCareCostController.onPageLoad")
    implicit request =>
      cacheClient.loadChildren().flatMap {
        case Some(children) =>
          redirectPage(children, index)
        case None =>
          Logger.debug(s"Childcare Cost Controller no children")
          Future.successful(Redirect(routes.HowManyChildrenController.onPageLoad()))
      } recover {
        case e : Exception =>
          Logger.warn(s"ChildCareCostController onPageLoad exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  def onSubmit(index: Int) = sessionProvider.withSession {
    Logger.debug(s"ChildcareCostController.onSubmit cacheClient: $cacheClient")
    implicit request =>

      ChildCareCostForm.form.bindFromRequest().fold(
        errors => {
          if(errors.errors.head.key == childCareCost) {
            onSubmitError(displayCost = true, index, errors, "cc.childcare.cost.error.required", "error.real")
          } else {
            onSubmitError(displayCost = false, index, errors, "cc.childcare.education.error.required", "error.boolean")
          }
        },
        success =>
          saveChild(success, index)
      )
  }

  private def onSubmitError(displayCost: Boolean, index: Int, errors: Form[_root_.models.pages.ChildCarePageModel], errorMessage: String, errorKey: String)(implicit request: Request[AnyContent]): Future[Result] = {
    Logger.debug(s"\n ChildCareCostController.onSubmitError Errors in ChildcareCostController onSubmit: $errors \n")
    val modified = formService.overrideFormError[_root_.models.pages.ChildCarePageModel](form = errors, key = errorKey, value = errorMessage)
    Future.successful(BadRequest(views.html.childCareCost(modified, showCost = displayCost, index)))
  }

}
