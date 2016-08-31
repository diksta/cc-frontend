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

package models.child

import mappings.Periods
import org.joda.time.LocalDate
import play.api.libs.json.Json

/**
 * Created by adamconder on 04/02/2016.
 */
case class Child(
                  id: Short, // what do we start at? 0?
                  name: String, // generated "Child (id)"
                  dob: Option[LocalDate],
                  disability: Disability,
                  childCareCost: Option[BigDecimal] = None,
                  childCareCostPeriod: Periods.Period = Periods.Monthly,
                  education: Option[Education] = None
                  )

object Child {
  implicit val formats = Json.format[Child]
}
