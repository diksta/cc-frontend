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

import org.joda.time.LocalDate
import play.api.libs.json.Json

/**
 * Created by adamconder on 04/02/2016.
 */

case class Education(
                  inEducation: Boolean = false,
                  startDate: Option[LocalDate] = None  /* is this needed? the screen on the frontend is actually a Boolean "Did you start before your 19th birthday Y/N" (also been removed)
                  we're going to have to determine a date for this to satisfy the TC microservice
                   Date needs to be: 1 day before the child's 19th birthday IF THE CHILD IS OLD ENOUGH >= 19 otherwise set null */
                  )

object Education {
  implicit val formats = Json.format[Education]
}
