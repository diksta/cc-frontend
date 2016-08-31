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

package service.keystore

/**
 * Created by adamconder on 18/11/14.
 */
trait CCKeystoreKeys {

  val tfcCalculatorResult : String = "tfc-calculator-result"
  val tcCalculatorResult : String = "tc-calculator-result"
  val escCalculatorResult : String = "esc-calculator-result"
  val tfcChildCareCostDetails :String = "tfc-childcare-cost-details"
  val numberOfChildren : String = "tfc-number-of-children"
  val houseHoldComposition : String = "tfc-household-composition"

  val childrenKey : String = "cc-children"
  val claimantKey : String = "cc-claimants"
  val householdKey : String = "cc-household"

}
