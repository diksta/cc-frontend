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

package form.validator

import formatter.CostFormatter
import play.api.Logger
import play.api.data.Mapping
import uk.gov.hmrc.play.validators.Validators

/**
 * Created by adamconder on 21/01/16.
 */

trait CostValidator extends Validators {
  import play.api.data.Forms._
  import play.api.data.format.Formats._

  /**
  -   * Constructs a mapping for a BigDecimal field.
-   *
-   * For example:
-   * {{{
-   * Form("montant" -> costDecimal(10, 2))
-   * }}}
-   * @param precision The maximun total number of digits (including decimals)
-   * @param scale The maximun number of decimalsS
-   */
  private def costDecimal(precision: Int, scale: Int): Mapping[BigDecimal] = {
    Logger.debug("calling costDecimal of CostValidator object")
    of[BigDecimal] as CostFormatter.costFormatter(Some((precision, scale)))
  }
  def cost : Mapping[BigDecimal] = costDecimal(4, 2)
}

object CostValidator extends CostValidator
