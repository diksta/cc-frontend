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
import controllers.manager.{HelperManager, ChildrenManager, FormManager}
import form.ChildDetailsForm
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object ChildDetailsController extends ChildDetailsController with CCSession with KeystoreService with ChildrenManager with FormManager with HelperManager{
  override val auditEvent = AuditEvents
}

trait ChildDetailsController extends FrontendController with HelperManager {

  this: CCSession with KeystoreService with ChildrenManager with FormManager =>

  val auditEvent : AuditEvents

  private val onNoChildRedirectToHowManyChildren : Call = routes.HowManyChildrenController.onPageLoad()

  private def costDetailsController(index: Int) : Call = routes.ChildCareCostController.onPageLoad(index)


  private def constructBackUrl(childrenList : List[_root_.models.child.Child], index : Int) = {
    Logger.debug(s"ChildDetailsController.constructBackUrl")
    if (index == 1) {
      routes.HowManyChildrenController.onPageLoad()
    }else {
      //previous child index
      val newIndex = index - 1
      val child = childrenService.getChildById(newIndex, childrenList)
      val today = LocalDate.now()
      val dob = child.dob

      val september1stForBirthday = september1stFollowingChildBirthday(dob.get)
      val childAge1stSept = age(Some(september1stForBirthday))
      val childAgeToday = age(dob)

      if (childrenService.childIsLessThan20YearsOld(childAgeToday)) {
        if ((childrenService.childIsAgedBetween15And16(childAge1stSept)) && child.disability.nonDisabled) {
          routes.ChildDetailsController.onPageLoad(newIndex)
        }
        else {
          routes.ChildCareCostController.onPageLoad(newIndex)
        }
      }
      else {
        routes.ChildDetailsController.onPageLoad(newIndex)
      }
    }
  }

  private def populateChildDetailsModel(child :  _root_.models.child.Child): _root_.models.pages.ChildDetailsPageModel = {
    Logger.debug(s"ChildDetailsController.populateChildDetailsModel")
    val  disability = _root_.models.pages.ChildDetailsDisabilityPageModel(
      disabled = child.disability.disabled,
      severelyDisabled = child.disability.severelyDisabled,
      certifiedBlind = child.disability.blind,
      nonDisabled = child.disability.nonDisabled
    )
    val childDetailsPageModel =  _root_.models.pages.ChildDetailsPageModel(
      dob = child.dob,
      disability = disability
    )
    childDetailsPageModel
  }

  private def saveChildDetails(index: Int, pageModel : _root_.models.pages.ChildDetailsPageModel)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    Logger.debug(s"ChildDetailsController.saveChildDetails")
    cacheClient.loadChildren().map {
      case Some(l) =>
        val disability = _root_.models.child.Disability(
          disabled = pageModel.disability.disabled,
          severelyDisabled = pageModel.disability.severelyDisabled,
          blind = pageModel.disability.certifiedBlind,
          nonDisabled = pageModel.disability.nonDisabled
        )
        val child = childrenService.getChildById(index, l)
        val modifiedChild = child.copy(dob = pageModel.dob,disability = disability)
        val updateList = childrenService.replaceChildInAList(l, index, modifiedChild)
        auditEvent.auditChildDetailsData((Json.toJson[_root_.models.child.Child](modifiedChild)).toString())
        cacheClient.saveChildren(updateList)
        Redirect(costDetailsController(index))

      case None =>
        Redirect(onNoChildRedirectToHowManyChildren)
    } recover {
      case e : Exception =>
        Logger.warn(s"ChildDetailsController.saveChildDetails exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onSubmit(index: Int) = sessionProvider.withSession {
    Logger.debug(s"ChildDetailsController.onSubmit ")
    implicit request =>

      ChildDetailsForm.form.bindFromRequest().fold(
        errors => {
          cacheClient.loadChildren().map {
            case Some(l) =>
              val backUrl =  constructBackUrl(l, index)
              val modified = formService.overrideFormError[_root_.models.pages.ChildDetailsPageModel](form = errors, key = "error.invalid.date.format", value = "cc.child.details.date.of.birth.mandatory")
              BadRequest(views.html.childDetails(modified, index,childrenService.getOrdinalNumber(index), backUrl))

            case None =>
              Redirect(onNoChildRedirectToHowManyChildren)
          }recover {
            case e : Exception =>
              Logger.warn(s"ChildDetailsController.onSubmit exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }

        },
        success =>
          saveChildDetails(index, success)
      )
  }

  def onPageLoad(index: Int) = sessionProvider.withSession {
    Logger.debug(s"ChildDetailsController.onPageLoad cacheClient: $cacheClient")
    implicit request =>
      cacheClient.loadChildren().map {
        case Some(l) =>
          try {
            val childDetailsModel = populateChildDetailsModel(childrenService.getChildById(index, l))
            val backUrl =  constructBackUrl(l, index)
            Ok(views.html.childDetails(ChildDetailsForm.form.fill(childDetailsModel), index, childrenService.getOrdinalNumber(index), backUrl))
          }
          catch{
            case e: Exception =>
              Logger.warn(s"ChildDetailsController.onPageLoad exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }
        case None =>
          Redirect(onNoChildRedirectToHowManyChildren)
      } recover {
        case e : Exception =>
          Logger.warn(s"ChildDetailsController.onPageLoad exception: ${e.getMessage}")
          sessionProvider.redirectLoadDifficulties
      }
  }
}
