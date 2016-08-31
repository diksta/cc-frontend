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

import config.ApplicationConfig
import models.pages.EmailRegisterPageModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages
import uk.gov.hmrc.emailaddress.EmailAddress


trait  EmailRegistrationFormKeys{
  val childrenDobSelection = "childrenDobSelection"
  val emailAddress = "emailAddress"
}

object EmailRegistrationForm extends EmailRegistrationForm

trait EmailRegistrationForm extends EmailRegistrationFormKeys {

  val emailRegex ="""^([a-zA-Z0-9\-\_]+[.])*[a-zA-Z0-9\-\_]+@([a-zA-Z0-9-]{2,}[.])+[a-zA-Z0-9-]+$""".r

  val emailConstraint : Constraint[String] = Constraint("constraints.email") ({
    text =>
      val trimmedEmail = text.trim
      if (trimmedEmail.length == 0)
        Invalid(Messages("cc.email.register.email.address.empty"))
      else if (EmailAddress.isValid(trimmedEmail)) {
        if (trimmedEmail.length > ApplicationConfig.emailAddressMaxLength)
          Invalid(Messages("cc.email.register.email.max.length"))
        else {
          emailRegex.findFirstMatchIn(trimmedEmail)
            .map(_ => Valid)
            .getOrElse(Invalid(Messages("cc.email.register.email.address.invalid")))
        }
      } 
      else
        Invalid(Messages("cc.email.register.email.address.invalid"))
  })

    val form : Form[EmailRegisterPageModel] = Form(
      mapping(
        emailAddress -> text.verifying(emailConstraint),
        childrenDobSelection -> optional(boolean).verifying(Messages("cc.email.register.dob.not.selected"), x => x.isDefined)
    )(EmailRegisterPageModel.apply)(EmailRegisterPageModel.unapply)
    )
}
