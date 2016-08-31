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

import connectors.CCAuthConnector
import controllers.keystore.{CCSession, SecuredConstants}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import scala.concurrent.Future


/**
 * Created by adamconder on 18/11/14.
 */

object CCController extends CCController with CCSession with CCAuthConnector {
  override val initialController = controllers.routes.HowManyChildrenController.onPageLoad()
}

trait CCController extends FrontendController {
  this: CCSession with CCAuthConnector =>
  val initialController : Call


  def onPageLoad = Action.async {

    implicit request =>
      Logger.debug(s"****** CCController.onPageLoad*******")
      sessionProvider.getSessionId match {
        case None =>
          redirectWithNewSession()
        case Some(SecuredConstants.NOSESSION) =>
          // controller has manually changed their session
          redirectWithNewSession()
        case _ =>
          redirectWithSession()
      }
  }

  private def redirectWithNewSession()(implicit request: Request[AnyContent]) = {
    // create a new session
    Logger.debug(s"****CCController.redirectWithNewSession****")
    val session = sessionProvider.generateSession()
    Future.successful(Redirect(initialController).withSession(session))
  }

  private def redirectWithSession()(implicit request: Request[AnyContent]) = {
    Logger.debug(s"*****CCController.redirectWithSession******")
    Future.successful(Redirect(initialController))
  }

  def loadDifficulties() = UnauthorisedAction.async {
    Logger.debug(s"*****CCController.loadDifficulties*****")

    implicit request =>
      Future.successful(InternalServerError(views.html.cccommon.technicalDifficulties()))
  }

}
