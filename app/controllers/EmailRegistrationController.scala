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

import _root_.config.ApplicationConfig
import connectors.EmailConnector
import controllers.keystore.CCSession
import controllers.manager.{ChildrenManager, FormManager, HelperManager}
import form.EmailRegistrationForm
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by ben on 25/05/16.
 */
object EmailRegistrationController extends EmailRegistrationController with CCSession with KeystoreService with ChildrenManager with FormManager with HelperManager {
  override val auditEvent = AuditEvents
  override val emailConnector = EmailConnector
}

trait EmailRegistrationController extends FrontendController {

  this: CCSession with KeystoreService with ChildrenManager with FormManager with HelperManager =>

  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  val firstSept2017 = LocalDate.parse("2017-09-01", formatter)

  val auditEvent : AuditEvents
  val emailConnector : EmailConnector

  //takes a sequence of children, extracts the DOB's and maps and flattens with flatMap,
  //sorts the sequence from youngest child to oldest
  //then returns the head of the sequence of DOB's in an Option[LocalDate] type
  private def findYoungestChild(childList: Seq[_root_.models.child.Child]): Option[List[LocalDate]] =
    Some(List(childList.flatMap(_.dob).sortWith(_ isAfter _).head))

  private def childrenStill16on1Sept107(dob: Option[LocalDate])  = {
    if(age(dob, currentDate = firstSept2017) > ApplicationConfig.tfcMaxAge)
      false
    else
      true
  }


  private def saveRegistrationDetails(pageModel: _root_.models.pages.EmailRegisterPageModel, freeEntitlement  :Boolean = false)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    Logger.debug(s"EmailRegisterController.saveRegistrationDetails: ")
    cacheClient.loadChildren().flatMap {
      case Some(children) =>
       //if free entitlement is available then store of the children's dob's where child is 16 as on 1st september 2017
        //  in all other cases  just find the youngest child
        val childrenDobList = (pageModel.childrenDobSelection.get, freeEntitlement) match {
          case (true, true) =>  auditEvent.auditAFHDOBSelected(true.toString)
                                  Some((children.filter(x => childrenStill16on1Sept107(x.dob))).flatMap(_.dob))
          case (true, false) =>   auditEvent.auditTFCDOBSelected(true.toString)
                                     (findYoungestChild(children))
          case (false, true) =>   auditEvent.auditAFHDOBSelected(false.toString)
                              None
          case (_,_)  =>   auditEvent.auditTFCDOBSelected(false.toString)
                             None
       }

        val emailAddress = pageModel.emailAddress
        val emailCapture =  _root_.models.email.EmailCapture(
          emailAddress = emailAddress,
          dob = childrenDobList,
          england = freeEntitlement
        )

        //invoke the email capture service
        emailConnector.submit(emailCapture).map {
          result =>
            result.status match {
              case OK =>
                auditEvent.auditEmailRegistrationDetails((Json.toJson[_root_.models.email.EmailCapture](emailCapture)).toString())
                Ok(views.html.emailConfirmation(emailAddress, freeEntitlement))
              case _ =>  Ok(views.html.emailUnavailable(request))
            }
        } recover {
          case e: Exception =>
            Logger.warn(s"EmailRegisterController: email capture connection exception: ${e.getMessage}")
            sessionProvider.redirectLoadDifficulties
        }

      case None =>
        Future.successful(Redirect(routes.HowManyChildrenController.onPageLoad()))
    } recover {
      case e: Exception =>
        Logger.warn(s"EmailRegisterController onSubmit exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }

  }

  def onPageLoad = sessionProvider.withSession {
    Logger.debug(s"EmailRegisterController.onPageLoad: $cacheClient")
    implicit request =>
         Future.successful(Ok(views.html.emailRegistration(EmailRegistrationForm.form)))
  }

  def onPageLoadFreeEntitlement = sessionProvider.withSession {
    Logger.debug(s"EmailRegisterController.onPageLoadFreeEntitlement: $cacheClient")
    implicit request =>
      Future.successful(Ok(views.html.emailRegistrationFreeEntitlement(EmailRegistrationForm.form)))
  }

  def onSubmit = sessionProvider.withSession {
    Logger.debug(s"EmailRegisterController.onSubmit: $cacheClient")
    implicit request =>
      EmailRegistrationForm.form.bindFromRequest().fold (
        errors =>
          Future.successful(BadRequest(views.html.emailRegistration(errors))),
        success =>
          saveRegistrationDetails(success)
      )
  }

  def onSubmitFreeEntitlement = sessionProvider.withSession {
    Logger.debug(s"EmailRegisterController.onSubmitFreeEntitlement: $cacheClient")
    implicit request =>
      EmailRegistrationForm.form.bindFromRequest().fold (
        errors =>
          Future.successful(BadRequest(views.html.emailRegistrationFreeEntitlement(errors))),
        success =>
          saveRegistrationDetails(success, freeEntitlement = true)
      )
  }

}
