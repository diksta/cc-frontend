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

import models.pages.{ChildDetailsDisabilityPageModel, ChildDetailsPageModel}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.play.mappers.DateTuple._


/**
 * Created by adamconder on 05/02/2016.
 */

trait DisabilityKeys {
  // name properties on the view
  val benefitDisabled = "benefitDisabled"
  val benefitSeverelyDisabled = "benefitSevereDisabled"
  val benefitBlind = "benefitBlind"
  val benefitNone = "benefitNone"
}

trait ChildDetailsKeys {
  val dob = "dateOfBirth"
  val disability = "disability"
}

object ChildDetailsForm extends ChildDetailsKeys with DisabilityKeys with ChildDetailsForm


trait ChildDetailsForm extends ChildDetailsKeys with DisabilityKeys {

  val disabilityMapping = mapping(
    benefitDisabled -> boolean,
    benefitSeverelyDisabled -> boolean,
    benefitBlind -> boolean,
    benefitNone -> boolean
  )(ChildDetailsDisabilityPageModel.apply)(ChildDetailsDisabilityPageModel.unapply)
    .verifying(Messages("cc.child.details.no.benefits.selected"), x => x.selection)
    .verifying(Messages("cc.child.details.invalid.benefits.selected"), x =>{ if (x.selection) x.validSelection else true})

  val form : Form[ChildDetailsPageModel] = Form(
    mapping(
      dob -> dateTuple(validate = true).verifying(Messages("cc.child.details.date.of.birth.mandatory"), data => {
        data match
        {
          case Some(x) =>
            if (x.year().get() < 0 || x.year().get().toString.length != 4)
              false
            else
              true
          case _ => false
        }}),
        disability -> disabilityMapping
    )(ChildDetailsPageModel.apply)(ChildDetailsPageModel.unapply)
  )
}
