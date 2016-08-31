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

package form

import controllers.manager.FormManager
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

/**
 * Created by user on 11/04/16.
 */
trait ClaimantLocationFormKeys{
  val whereDoYouLive = "whereDoYouLive"
}

object ClaimantLocationForm extends ClaimantLocationFormKeys with FormManager {

  type ClaimantLocationFormType = Option[String]

  val form = Form[ClaimantLocationFormType](
    mapping(
      whereDoYouLive -> optional(text).verifying(Messages("cc.claimant.where.do.you.live.no.selection"), x => x.isDefined)
    )((whereDoYouLive) => whereDoYouLive)((whereDoYouLive : ClaimantLocationFormType) => Some(whereDoYouLive))
  )

}
