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

package controllers.keystore

import java.util.UUID

import controllers.routes
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

object SecuredConstants {
  val NOSESSION = "NOSESSION"
}

trait CCSession {
    val sessionProvider = new CCSessionProvider
}

class CCSessionProvider {
  // construct a new session with the old session data 
  def generateSession()(implicit request: Request[AnyContent]) = {
    val newId = generateSessionId()
    val session = Session(request.session.data ++ Map(newId))
    session
  }

  def generateSessionId() : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def getSessionId()(implicit request : Request[AnyContent]) = request.session.get(SessionKeys.sessionId)

  def onUnauthorized = Results.Redirect(routes.CCController.onPageLoad())

  def futureRequest(result: Result) = Future.successful(result)

  def redirectLoadDifficulties = {
    Results.Redirect(controllers.routes.CCController.loadDifficulties())
  }

  /**
   * every session should have an ID: required by key-store
   * If no session Id is found or session was deleted (NOSESSION), user is redirected to welcome page, where new session id will be issued on submit
   * @return redirect to required page
   */

  def withSession(f: => Request[AnyContent]=> Future[Result]) : Action[AnyContent] = {
    Logger.debug(s"CCSessionProvider.withSession ")
    Action.async {
      implicit request : Request[AnyContent] =>
        getSessionId match {
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          case Some(SecuredConstants.NOSESSION) => futureRequest(onUnauthorized) // Go to the homepage
          // $COVERAGE-ON
          case None => futureRequest(onUnauthorized) // Go to the homepage
          case _ => f(request) // Carry on
        }
    }
  }
}
