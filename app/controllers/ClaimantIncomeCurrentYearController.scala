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
import controllers.manager.{ClaimantManager, FormManager, HelperManager}
import form.ClaimantIncomeCurrentYearFormInstance
import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
import service.AuditEvents
import service.keystore.KeystoreService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by user on 03/03/16.
 */

object ClaimantIncomeCurrentYearController extends ClaimantIncomeCurrentYearController with CCSession with KeystoreService with ClaimantManager  with FormManager with HelperManager {
  override val auditEvent = AuditEvents
}

trait ClaimantIncomeCurrentYearController extends FrontendController {

  this: CCSession with KeystoreService with ClaimantManager with FormManager with HelperManager =>

  val auditEvent : AuditEvents

  private def createCurrentIncomeModel(pageModel : _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel) = {
    Logger.debug(s"ClaimantIncomeCurrentYearController.createCurrentIncomeModel")

    val currentIncomeSelection = pageModel.selection match {
      case Some(true) => true
      case _ => false
    }

    if(currentIncomeSelection){
      val currentIncome = Some(_root_.models.claimant.Income(
        employmentIncome = if (pageModel.employment.selection) pageModel.employment.income else None,
        pension = if (pageModel.employment.selection) pageModel.employment.pension else None,
        benefits = if (pageModel.benefits.selection) pageModel.benefits.amount else None,
        otherIncome = if (pageModel.other.selection) pageModel.other.income else None
      ))
      currentIncome
    }
    else {
      Some(_root_.models.claimant.Income(None,None,None,None))
    }
  }

  private def saveClaimantCurrentIncome(claimantList : List[_root_.models.claimant.Claimant], pageModel : _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel, index : Short)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
    Logger.debug(s"ClaimantIncomeCurrentYearController.saveClaimantCurrentIncome")
    val claimant = claimantService.getClaimantById(claimantList, index)
    val newCurrentIncome = createCurrentIncomeModel(pageModel)
    val modifiedClaimant = claimant.copy(currentIncome = newCurrentIncome)
    val modifiedList = claimantService.replaceClaimantInAList(claimantList,modifiedClaimant,index)
    cacheClient.saveClaimants(modifiedList).map {
      result =>
        if(index == 1) {
          auditEvent.auditClaimantCurrentYearIncomeData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
          Redirect(controllers.routes.ClaimantHoursController.onPageLoadParent())
        } else {
          auditEvent.auditPartnerCurrentYearIncomeData((Json.toJson[_root_.models.claimant.Claimant](modifiedClaimant)).toString())
          Redirect(controllers.routes.ClaimantHoursController.onPageLoadPartner())
        }
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantIncomeCurrentYearController.saveClaimantCurrentIncome exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def isCarerAllowanceBenefitsError(errors : Seq[FormError]) = {
    !(errors.filter(x =>
      if((x.message == Messages("cc.claimant.income.current.year.parent.benefits.carers.selected")) || (x.message == Messages("cc.claimant.income.current.year.partner.benefits.carers.selected")))
        true
      else
        false
    )).isEmpty

  }

  private def overrideBenefitSelection(form: Form[_root_.models.pages.income.ClaimantIncomeCurrentYearPageModel]) = {
    //make the selections true in the map
    val modifiedData = form.data + ("selection" -> "true") +  ("benefits.selection" -> "true")
    form.copy(form.mapping, modifiedData, form.errors, form.value)

  }

  private def saveClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    Logger.debug(s"ClaimantIncomeCurrentYearController.saveClaimant")

    cacheClient.loadClaimants().flatMap {
      case Some(l) =>
        val claimant = claimantService.getClaimantById(l, index.toInt)

        val isPartner = if(index == 1) false else true
        new ClaimantIncomeCurrentYearFormInstance(isPartner, claimant.disability.carersAllowance, claimant.previousIncome, currentYearSelection = ((request.body.asFormUrlEncoded.get).get("selection")).isDefined).form.bindFromRequest().fold(
          errors => {
            val fromToTaxYear = determineTaxYearFromNow(LocalDate.now())
            val modified = formService.overrideFormError[_root_.models.pages.income.ClaimantIncomeCurrentYearPageModel](form = errors, value = "cc.claimant.income.current.year.error", key = "error.real")
            val hasCarrerAllowanceErrorOccured = isCarerAllowanceBenefitsError(modified.errors)
            // If there is carer's allowance error reload the form with the selection has Yes and Benefits checkbox ticked
            val modifiedFormBenefitChanges = if (hasCarrerAllowanceErrorOccured) overrideBenefitSelection(modified) else modified

            Future.successful(BadRequest(views.html.incomeCurrentYear(modifiedFormBenefitChanges, index, taxYearFrom = fromToTaxYear.toString, taxYearTo = (fromToTaxYear+1).toString, claimant.disability.carersAllowance)))
          },
          success =>
            saveClaimantCurrentIncome(l, success, index)
        )
      case _ =>
        Future.successful(sessionProvider.redirectLoadDifficulties)
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantIncomeCurrentYearController.saveClaimant exception: ${e.getMessage}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  private def populatePageModel(claimantList : List[_root_.models.claimant.Claimant], index: Short): Option[_root_.models.pages.income.ClaimantIncomeCurrentYearPageModel] =  {
    Logger.debug(s"ClaimantIncomeCurrentYearController.populatePageModel")

    val claimant = claimantService.getClaimantById(claimantList, index)
    claimant.currentIncome match {
      case Some(x) =>
        val claimantIncomeCurrentYearPageModel = _root_.models.pages.income.ClaimantIncomeCurrentYearPageModel(
          selection = Some((x.employmentIncome.isDefined || x.otherIncome.isDefined || x.benefits.isDefined)),
          employment =  _root_.models.pages.income.ClaimantIncomeCurrentYearEmploymentPageModel(
            selection = x.employmentIncome.isDefined || x.pension.isDefined,
            income = x.employmentIncome,
            pension = x.pension
          ),
          other = _root_.models.pages.income.ClaimantIncomeCurrentYearOtherPageModel(
            selection = x.otherIncome.isDefined,
            income = x.otherIncome
          ),
          benefits = _root_.models.pages.income.ClaimantIncomeCurrentYearBenefitsPageModel(
            selection = x.benefits.isDefined,
            amount = x.benefits
          )
        )
        Some(claimantIncomeCurrentYearPageModel)
      case _ =>
        None

    }
  }

  private def loadClaimant(index : Short) (implicit hc : HeaderCarrier, request : Request[AnyContent])  = {
    Logger.debug(s"ClaimantIncomeCurrentYearController.loadClaimant: $cacheClient")
    cacheClient.loadClaimants.map {
      case Some(l) =>
        val pageModel = populatePageModel(l, index)
        val taxYears = determineTaxYearFromNow(LocalDate.now())
        val isPartner = if(index == 1) false else true
        pageModel match {
          case Some(x) =>
            Ok(views.html.incomeCurrentYear(new ClaimantIncomeCurrentYearFormInstance(isPartner).form.fill(x), index, taxYears.toString, (taxYears+1).toString, false))
          case _ =>
            Ok(views.html.incomeCurrentYear(new ClaimantIncomeCurrentYearFormInstance(isPartner).form, index, taxYears.toString, (taxYears+1).toString, false))
        }
      case None  =>
        Redirect(controllers.routes.ClaimantBenefitsController.onPageLoadParent())
    } recover {
      case e : Exception =>
        Logger.warn(s"ClaimantIncomeCurrentYearController.onPageLoad exception: ${e.getMessage} trace: ${e.printStackTrace()}")
        sessionProvider.redirectLoadDifficulties
    }
  }

  def onPageLoadParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeCurrentYearController.onPageLoadParent")
    implicit request =>
      loadClaimant(1)
  }

  def onSubmitParent = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeCurrentYearController.onSubmitParent")
    implicit request =>
      saveClaimant(1)
  }

  def onPageLoadPartner = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeCurrentYearController.onPageLoadPartner")
    implicit request =>
      loadClaimant(2)
  }

  def onSubmitPartner = sessionProvider.withSession {
    Logger.debug(s"ClaimantIncomeCurrentYearController.onSubmitPartner")
    implicit request =>
      saveClaimant(2)
  }

}
