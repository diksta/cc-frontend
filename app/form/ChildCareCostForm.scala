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

import form.validator.CostValidator
import models.pages.ChildCarePageModel
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

/**
 * Created by user on 04/02/2016.
 */

trait ChildCareCostKeys {
  val childCareCost = "childCareCost"
  val childEducation = "childEducation"
}

object ChildCareCostForm extends ChildCareCostKeys with ChildCareCostForm

trait ChildCareCostForm extends ChildCareCostKeys {

  val form : Form[ChildCarePageModel] = Form(
    mapping(
      childCareCost -> optional(CostValidator.cost).verifying(Messages("cc.childcare.cost.error.required"), x => x.isDefined && x.nonEmpty),
      childEducation -> optional(boolean).verifying(Messages("cc.childcare.education.error.required"), x => x.isDefined)
    )(ChildCarePageModel.apply)(ChildCarePageModel.unapply)
  )
}
