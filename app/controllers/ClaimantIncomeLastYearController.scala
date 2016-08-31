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
import controllers.manager.{FormManager, HelperManager, ClaimantManager}
import form.{ClaimantIncomeLastYearFormInstance, ClaimantIncomeCurrentYearForm, ClaimantIncomeLastYearForm}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.helper.form

import scala.concurrent.Future

/**
 * Created by user on 23/02/16.
 */

object ClaimantIncomeLastYearController extends ClaimantIncomeLastYearController with CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager {
  override val auditEvent = AuditEvents
}

trait ClaimantIncomeLastYearController extends FrontendController {

  this: CCSession with KeystoreService with ClaimantManager with HelperManager with FormManager =>

  val auditEvent : AuditEvents

  private def populatePageModel(claimantList : List[_root_.models.claimant.Claimant], index: Short): Option[_root_.models.pages.income.ClaimantIncomeLastYearPageModel] =  {
    Logger.debug(s"ClaimantIncomeLastYearController.populatePageModel")
    val claimant = claimantService.getClaimantById(claimantList, index)

    claimant.previousIncome match {
      case Some(x) =>
        val claimantIncomeLastYearPageModel = _root_.models.pages.income.ClaimantIncomeLastYearPageModel(
          employment =  _root_.models.pages.income.ClaimantIncomeLastYearEmploymentPageModel(
            selection = x.isEmploymentIncomeSelected(),
            income = x.employmentIncome,
            pension = x.pension
          ),
          other =  _root_.models.pages.income.ClaimantIncomeLastYearOtherPageModel(
            selection = x.isOtherIncomeSelected(),
            income = x.otherIncome
          ),
          benefits =  _root_.models.pages.income.ClaimantIncomeLastYearBenefitsPageModel(
            selection = x.isBenefitsSelected(),
            amount = x.benefits
          )
        )
        Some(claimantIncomeLastYearPageModel)
      case _ =>
        None

    }
  }

  private def createPreviousIncomeModel(pageModel : _root_.models.pages.income.ClaimantIncomeLastYearPageModel) = {
    Logger.debug(s"ClaimantIncomeLastYearController.createPreviousIncomeModel")

    val employmentSelection = pageModel.employment.selection match {
      case Some(true) => true
      case _ => false
    }

    val benefitsSelection = pageModel.benefits.selection match {
      case Some(true) => true
      case _ => false
    }

    val otherSelection = pageModel.other.selection match {
      case Some(true) => true
      case _ => false
    }

    if (!employmentSelection && !benefitsSelection && !otherSelection){
      val previousIncome = Some(_root_.models.claimant.Income(
        employmentIncome = None,
        pension = None,
        benefits = None,
        otherIncome = None
      ))
      previousIncome
    }
    else  {
      val previousIncome = Some(_root_.models.claimant.Income(
        employmentIncome = if (employmentSelection) pageModel.employment.income else None,
        pension = if (employmentSelection) pageModel.employment.pension else None,
        benefits = if (benefitsSelection) pageModel.benefits.amount else None,
        otherIncome = if (otherSelection) pageModel.other.income else None
      ))
      previousIncome
    }
  }

  private def saveClaimantPreviousIncome(claimantList : List[_root_.models.claimant.Claimant], pageModel : _root_.models.pages.income.ClaimantIncomeLastYearPageModel, index : Short)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {

    Logger.debug(s"ClaimantIncomeLastYearController.saveClaimantPreviousIncome")
    val claimant = claimantService.getClaimantById(claimantList, index)
    val newPreviousIncome = createPreviousIncomeModel(pageModel)
    val modifiedClaimant = claimant.copy(previousIncome = newPreviousIncome)
    val modifiedList = claimantService.replaceClaimantInAList(claimantList,modifiedClaimant,index)
    cacheClient.saveClaimants(modifiedList).map {
      result =>
        if (index == 1) {//index 1 parent
        auditEvent.auditClaimantLastYearIncomeData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
        Redirect(controllers.routes.ClaimantIncomeCurrentYearController.onPageLoadParent())
    }
    else {
         //index 2 partner
        auditEvent.auditPartnerLastYearIncomeData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
        Redirect(controllers.routes.ClaimantIncomeCurrentYearController.onPageLoadPartner())
      }
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantIncomeLastYearController.saveClaimantPreviousIncome exception : ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def saveClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    Logger.debug(s"ClaimantIncomeLastYearController.saveClaimant")

    new ClaimantIncomeLastYearFormInstance(partner = if(index == 2) true else false).form.bindFromRequest().fold(
      errors => {
        val fromToTaxYear = determineLastTaxYear(LocalDate.now())
        Logger.debug(s"errors: ${errors.errors}")

        val modified = formService.overrideFormError[_root_.models.pages.income.ClaimantIncomeLastYearPageModel](form = errors, value = "cc.claimant.income.last.year.error", key = "error.real")
        Future.successful(BadRequest(
          views.html.incomeLastYear(modified, taxYears = (fromToTaxYear._1.toString, fromToTaxYear._2.toString), index)
        ))
      },
      success =>
        cacheClient.loadClaimants().flatMap {
          case Some(l) =>
            saveClaimantPreviousIncome(l, success, index)
          case _ =>
            Future.successful(sessionProvider.redirectLoadDifficulties)
        } recover {
          case e : Exception =>
            Logger.warn(s"ClaimantIncomeLastYearController.saveClaimant exception : ${e.getMessage}")
            sessionProvider.redirectLoadDifficulties
        }
    )
  }

  private def loadClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    Logger.debug(s"ClaimantIncomeLastYearController cacheClient: $cacheClient")
    cacheClient.loadClaimants.map {
      case Some(l) =>
        val fromToTaxYear = determineLastTaxYear(LocalDate.now())
        val pageModel = populatePageModel(l, index)
        pageModel match {
          case Some(x) =>
            Ok(views.html.incomeLastYear((new ClaimantIncomeLastYearFormInstance).form.fill(x), taxYears = (fromToTaxYear._1.toString, fromToTaxYear._2.toString), index))
          case _ =>
            Ok(views.html.incomeLastYear((new ClaimantIncomeLastYearFormInstance).form, taxYears = (fromToTaxYear._1.toString, fromToTaxYear._2.toString), index))
        }
      case None  =>
        if(index == 1) //index 1 is parent
          Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
        else
          Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadPartner())
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantIncomeLastYearController.loadClaimant exception: ${e.getMessage} trace: ${e.printStackTrace()}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onPageLoadParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeLastYearController.onPageLoadParent")
    implicit request =>
      loadClaimant(1)
  }

  def onSubmitParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeLastYearController.onSubmitParent")
    implicit request =>
      saveClaimant(1)
  }

  def onPageLoadPartner = sessionProvider.withSession {
    implicit request =>
      loadClaimant(2)
  }

  def onSubmitPartner = sessionProvider.withSession {
    implicit request =>
      saveClaimant(2)
  }

}
