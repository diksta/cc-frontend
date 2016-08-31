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

package controllers

import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.mvc.Results
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 29/01/2016.
 */
class DevAssetsSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val mockDevAssets = DevAssets
  implicit val request = FakeRequest()

  "DevAssets" when {

    "is provided a valid file" should {

      "return a new File() using the absolute path" in {
        val result = await(mockDevAssets.at("conf/", "messages")(request))
        status(result) shouldBe Status.OK
      }

      "return a new File() when not using the absolute path" in {
        val result = await(mockDevAssets.at("", "/conf/application.conf")(request))
        status(result) shouldBe Status.OK
      }

    }

    "the file does not exist" should {

      "return NotFound for the requested File()" in {
        val result = await(mockDevAssets.at("/", "file_that_does_not_exist")(request))
        result.toString() shouldBe Results.NotFound.toString()
      }

    }

  }
}
