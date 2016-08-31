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

trait ESCVouchersFormKeys{
  val doYouGetVouchers = "doYouGetVouchers"
}

trait ESCVouchersForm extends ESCVouchersFormKeys with FormManager{

  type ClaimantVoucherFormType = Option[String]

  val isParent : Boolean

  val form = Form[ClaimantVoucherFormType](
    mapping(
    doYouGetVouchers -> optional(text).verifying(if (isParent)Messages("cc.do.you.get.vouchers.selection") else Messages("cc.do.they.get.vouchers.selection") ,x => x.isDefined)
    )((doYouGetVouchers) => doYouGetVouchers)((doYouGetVouchers : ClaimantVoucherFormType) => Some(doYouGetVouchers)
    )
  )

}
class ESCVouchersFormInstance(parent : Boolean = true) extends ESCVouchersForm {
  override val isParent = parent
}
