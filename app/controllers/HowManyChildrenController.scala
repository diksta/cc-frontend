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
import controllers.manager.{ChildrenManager, FormManager}
import form.HowManyChildrenForm
import play.api.Logger
import play.api.mvc.{AnyContent, Call, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by adamconder on 05/02/2016.
 */

object HowManyChildrenController extends HowManyChildrenController with CCSession with KeystoreService with ChildrenManager with FormManager {
  override val auditEvent = AuditEvents
}

trait HowManyChildrenController extends FrontendController {
  this: CCSession with KeystoreService with ChildrenManager with FormManager =>

  val auditEvent : AuditEvents

  private val onSubmitRedirect : Call = routes.ChildDetailsController.onPageLoad(index = 1)

  private def saveChildren(children : List[_root_.models.child.Child])(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    Logger.debug(s"HowManyChildrenController.saveChildren: ")
    auditEvent.auditHowManyChildrenData(children.size.toString())
    cacheClient.saveChildren(children).map {
      result =>
        Redirect(onSubmitRedirect)
    } recover {
      case e : Exception =>
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onPageLoad = sessionProvider.withSession {
    Logger.debug(s"HowManyChildrenController.onPageLoad: $cacheClient")
    implicit request =>
      cacheClient.loadChildren().map {
        case Some(l) =>
          val numberOfChildren = l.size
          Ok(views.html.howManyChildren(HowManyChildrenForm.form.fill(Some(numberOfChildren.toString))))
        case None =>
          Ok(views.html.howManyChildren(HowManyChildrenForm.form))
      } recover {
        case e : Exception =>
          Logger.warn(s"HowManyChildrenController.onPageLoad exception: ${e.getMessage} trace: ${e.printStackTrace()}")
          sessionProvider.redirectLoadDifficulties
      }
  }

  def onSubmit = sessionProvider.withSession {
    Logger.debug(s"HowManyChildrenController.onSubmit: $cacheClient")
    implicit request =>
      HowManyChildrenForm.form.bindFromRequest().fold(
        errors => {
          val modified = formService.overrideFormError[Option[String]](form = errors, key = "error.number", value = "cc.how.many.children.error.not.a.number")
          Future.successful(
            BadRequest(views.html.howManyChildren(modified))
          )
        },
        success =>
          cacheClient.loadChildren().flatMap {
            case Some(l) =>
              // modify existing list
              val modified = childrenService.modifyListOfChildren(requiredNumberOfChildren = success.get.trim.toInt, children = l)
              saveChildren(modified)
            case None =>
              // create a new list
              val children = childrenService.createListOfChildren(success.get.trim.toInt)
              saveChildren(children)
          } recover {
            case e : Exception =>
              Logger.warn(s"HowManyChildrenController.onSubmit exception: ${e.getMessage}")
              sessionProvider.redirectLoadDifficulties
          }
      )
  }

}
