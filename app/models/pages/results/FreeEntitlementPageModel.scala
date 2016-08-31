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

package models.pages.results

import play.api.libs.json.Json

/**
 * Created by ben on 27/04/16.
 */
case class FreeEntitlementPageModel (
                                    twoYearOld : Boolean,
                                    threeYearOld : Boolean,
                                    fourYearOld : Boolean,
                                    threeFourYearOldSep2017 : Boolean,
                                    region : String,
                                    tfcEligibility : Boolean = false
                                      )

object FreeEntitlementPageModel {
  implicit val formats = Json.format[FreeEntitlementPageModel]
}
