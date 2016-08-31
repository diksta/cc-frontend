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

import models.payload.calculator.input.tfc.TFCEligibility
import models.payload.eligibility.output.EligibilityOutput
import models.payload.eligibility.output.tfc.TFCOutputChild
import models.payload.eligibility.output.tfc.TFCPeriod
import org.joda.time.LocalDate
import play.api.Logger

/**
 * Created by user on 29/03/16.
 */

trait PayloadTFCCalculatorService {

   def createTFCCalculatorPayload(eligibilityOutput: EligibilityOutput, childrenInput : List[_root_.models.child.Child]): TFCEligibility = {
     Logger.debug(s"PayloadTFCCalculatorService.createTFCCalculatorPayload")
    val tfcEligibilityOutput = eligibilityOutput.eligibility.tfc.get
    val tfcPeriods = createTFCPeriod(tfcEligibilityOutput.periods, childrenInput)
    TFCEligibility(
      from = tfcEligibilityOutput.from,
      until = tfcEligibilityOutput.until,
      householdEligibility = tfcEligibilityOutput.householdEligibility,
      periods = tfcPeriods
    )
  }

   private def createTFCPeriod(periods : List[TFCPeriod], childrenInput : List[_root_.models.child.Child]) = {
     Logger.debug(s"PayloadTFCCalculatorService.createTFCPeriod")
     for (period <- periods) yield {
       val tfcChildren = createTFCChildren(period.children, childrenInput)
       models.payload.calculator.input.tfc.TFCPeriod (
         from = period.from,
         until = period.until,
         periodEligibility = period.periodEligibility,
         children = tfcChildren
       )
     }
   }

   private def createTFCChildren(children : List[TFCOutputChild], childrenInput : List[_root_.models.child.Child]): List[models.payload.calculator.input.Child] = {
     Logger.debug(s"PayloadTFCCalculatorService.createTFCChildren")
     for(child <- children) yield {
       val childDisability = createTFCChildDisability(child.id, childrenInput)
       val childCareCost = getChildCareCost(child.id, childrenInput)
       models.payload.calculator.input.Child(
           id = child.id,
           name = child.name,
           qualifying = child.qualifying,
           from = getChildDate(child.from),
           until = getChildDate(child.until),
           childcareCost = childCareCost,
           disability = childDisability
       )
     }
   }

  private def getChildCareCost(id: Short, childrenInput : List[_root_.models.child.Child]) = {
    Logger.debug(s"PayloadTFCCalculatorService.getChildCareCost")
    childrenInput.foldLeft(BigDecimal(0.00))((acc, child) => {
      if((id == child.id) && (child.childCareCost.isDefined))
        acc + child.childCareCost.get
      else
        acc
    })
  }

  private def createTFCChildDisability(id: Short, childrenInput : List[_root_.models.child.Child]): models.payload.calculator.input.Disability = {
    Logger.debug(s"PayloadTFCCalculatorService.createTFCChildDisability")
    var disability = models.payload.calculator.input.Disability(
      disabled = false,
      severelyDisabled = false
    )
    for(child <- childrenInput) yield {
      if(child.id == id) {
        disability = models.payload.calculator.input.Disability(
          disabled = child.disability.blind || child.disability.disabled,
          severelyDisabled = child.disability.severelyDisabled
        )
      }
    }
    disability
  }

  private def getChildDate(date : Option[LocalDate]): LocalDate = {
    Logger.debug(s"PayloadTFCCalculatorService.getChildDate")
    date match {
      case Some(x) => x
      case _ => null
    }
  }

 }
