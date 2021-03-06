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
trait DoYouLiveWithPartnerFormKeys{
  val doYouLiveWithYourPartner = "doYouLiveWithYourPartner"
}

object DoYouLiveWithPartnerForm extends DoYouLiveWithPartnerFormKeys with FormManager {

  type DoYouHavePartnerFormType = Option[Boolean]

  val form = Form[DoYouHavePartnerFormType](
    mapping(
      doYouLiveWithYourPartner -> optional(boolean).verifying(Messages("cc.do.you.live.with.partner.no.selection"), x => x.isDefined)
    )((doYouLiveWithYourPartner) => doYouLiveWithYourPartner)((doYouLiveWithYourPartner : DoYouHavePartnerFormType) => Some(doYouLiveWithYourPartner))
  )

}
