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

package service

import play.api.mvc.Request
import uk.gov.hmrc.play.http.HeaderCarrier


/**
 * Created by user on 25/04/16.
 */
object AuditEvents extends AuditEvents {

  override val auditService = AuditService
  override val auditDataHelper = AuditDataHelper
}

trait AuditEvents {

  val auditService : AuditService
  val auditDataHelper : AuditDataHelper

  def auditHowManyChildrenData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("numberOfChildren", Map("numberOfChildren" -> data))
  }

  def auditChildDetailsData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("childDetails", Map("childDetails" -> data))
  }

  def auditCostAndEducationsData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("costAndEducation", Map("costAndEducation" -> data))
  }

  def auditClaimantBenefitsData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantBenefits", Map("claimantBenefits" -> data))
  }

  def auditClaimantLastYearIncomeData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantLastYearIncome", Map("claimantLastYearIncome" -> data))
  }

  def auditClaimantCurrentYearIncomeData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantCurrentYearIncome", Map("claimantCurrentYearIncome" -> data))
  }

  def auditClaimantHoursData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantHowManyHoursDoYouWork", Map("claimantHowManyHoursDoYouWork" -> data))
  }

  def auditClaimantEscVouchersAvailable(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantEscVouchersAvailable", Map("claimantEscVouchersAvailable" -> data))
  }

  def auditClaimantLocation(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("claimantLocation", Map("claimantLocation" -> data))
  }

  def auditDoYouLiveWithPartnerData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("doYouLiveWithPartner", Map("doYouLiveWithPartner" -> data))
  }

  def auditHouseholdBenefits(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("householdBenefits", Map("householdBenefits" -> data))
  }

  def auditResultPageDetails(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("resultPageDetails", Map("resultPageDetails" -> data))
  }
  def auditResultSummary(auditData : Tuple10[String, String, String, String, String, String, String, String, String, String],
                         claimants : List[_root_.models.claimant.Claimant],
                         children: List[_root_.models.child.Child])
                        (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    val data = auditDataHelper.getResultSummaryAuditData(auditData,claimants, children)
    auditEvent("resultSummary", data)
  }

  def auditClaimantChildrenBenefits(claimants : List[_root_.models.claimant.Claimant],
                                    children: List[_root_.models.child.Child]) (implicit request: Request[_],
                                     hc: HeaderCarrier): Unit = {
    val data = auditDataHelper.getClaimantChildrenBenefitsAuditData(claimants, children)
    auditEvent("claimantChildrenBenefits", data)
  }

  def auditCostPerAge(children : List[_root_.models.child.Child]) (implicit request: Request[_],
                                                                  hc: HeaderCarrier): Unit = {
    val data = auditDataHelper.getChildcareCostPerAgeAuditData(children)
    if(data.nonEmpty)
      auditEvent("costsPerAge", data)
  }

  def auditEmailRegistrationDetails(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("emailRegistrationDetails", Map("emailRegistrationDetails" -> data))
  }

  def auditPartnerBenefitsData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("partnerBenefits", Map("partnerBenefits" -> data))
  }

  def auditPartnerLastYearIncomeData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("partnerLastYearIncome", Map("partnerLastYearIncome" -> data))
  }

  def auditPartnerCurrentYearIncomeData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("partnerCurrentYearIncome", Map("partnerCurrentYearIncome" -> data))
  }

  def auditPartnerHoursData(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("partnerHowManyHoursDoYouWork", Map("partnerHowManyHoursDoYouWork" -> data))
  }

  def auditPartnerEscVouchersAvailable(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("partnerEscVouchersAvailable", Map("partnerEscVouchersAvailable" -> data))
  }

  def auditAFHDOBSelected(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("AFHDOBSelected", Map("AFHDOBSelected" -> data))
  }

  def auditTFCDOBSelected(data: String) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditEvent("TFCDOBSelected", Map("TFCDOBSelected" -> data))
  }

  private def auditEvent(auditEventType : String, data: Map[String, String]) (implicit request: Request[_], hc: HeaderCarrier): Unit = {
    auditService.sendEvent(auditEventType, data)
  }

}
