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
import controllers.manager.FormManager
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

/**
 * Created by adamconder on 04/02/2016.
 */
trait HowManyChildrenKeys {
  val numberOfChildren =  "numberOfChildren"
}

object HowManyChildrenForm extends HowManyChildrenKeys with FormManager {

  val regex = "([0-9 ]{1,})".r

  type HowManyChildrenFormType = Option[String]

  val form = Form[HowManyChildrenFormType](
    mapping(
      numberOfChildren -> optional(text)
        .verifying(Messages("cc.how.many.children.error.required"), x => x.isDefined && x.nonEmpty)
        .verifying(Messages("cc.how.many.children.error.not.a.number"), x => x match {
        case Some(v) =>
          if (v.matches(regex.toString())) {
            val lengthCheck = ApplicationConfig.numberOfChildrenMaxLength
            val maximumNumber = ApplicationConfig.maximumNumberOfChildren
            val minimumNumber = ApplicationConfig.minimumNumberOfChildren

            val modified = formService.removeLeadingZero(v)

            try {
              // regex still allows two spaces so therefore have to catch the exception of .toInt
              modified.trim.length < lengthCheck && modified.trim.toInt > minimumNumber && modified.trim.toInt < maximumNumber
            } catch {
              case e : Exception =>
                false
            }
          } else {
            false
          }
        case None => true
      })
    )((numberOfChildren) => numberOfChildren)((numberOfChildren : HowManyChildrenFormType) => Some(numberOfChildren))
  )

}
