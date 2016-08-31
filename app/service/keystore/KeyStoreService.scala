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

/* def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[T] = {
    Logger.info(s"SessionCache: caching to keystore: key:$key")
    sessionCache.cache[T](key, data) map {
      cacheMap =>
        data
    }
  }

  def fetchAndGetFormData[T](formId: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry[T](key = formId)
  }

  def clearCache()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }*/


package service.keystore

import config.CCSessionCache
import play.api.Logger
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.logging.{LoggingDetails, MdcLoggingExecutionContext}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}

trait KeystoreService extends CCKeystoreKeys {

  val cacheClient = new ChildcareKeystoreService
  val sessionCache: SessionCache = CCSessionCache

  class ChildcareKeystoreService {

    val source = "ccfrontend"

    implicit def mdcExecutionContext(implicit loggingDetails: LoggingDetails): ExecutionContext = MdcLoggingExecutionContext.fromLoggingDetails
    /**
     * Store data to Keystore using a key
     */
    def cacheEntryForSession[T](data: T,key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      Logger.info(s"SessionCache: caching to keyStore: key:$key")
      sessionCache.cache[T](source, buildId, key, data) map {
        case x => x.getEntry[T](key)
      }
    }

    /**
     * get particular key out of keystore
     */
    def fetchEntryForSession[T](key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      Logger.info(s"SessionCache: fetching from keyStore:  key:$key")
      sessionCache.fetchAndGetEntry[T](source, buildId, key)
    }

    //This will append a session id or similar to construct a unique id for this user
    def buildId(implicit request: Request[Any]) = {
      val id = "cc_pages"
      val sessionId = request.session.get(SessionKeys.sessionId)

      sessionId match {
        case Some(_) =>
          val sSessionId = sessionId.get
          s"$id:$sSessionId"
        case _ =>
          "noSessionIdFound"
      }

    }

    def loadChildren()(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      fetchEntryForSession[List[models.child.Child]](childrenKey).map {
        result =>
          Logger.debug(s"loadChildren: $result")
          result
      }
    }

    def saveChildren(children : List[models.child.Child])(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      Logger.debug(s"saveChildren in KeyStore: $children")
      cacheEntryForSession[List[models.child.Child]](children, childrenKey).map {
        result =>
          Logger.debug(s"saveChildren: $result")
          result
      }
    }

    def loadClaimants()(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      fetchEntryForSession[List[_root_.models.claimant.Claimant]](claimantKey).map {
        result =>
          Logger.debug(s"loadClaimants: $result")
          result
      }
    }

    def saveClaimants(claimants : List[_root_.models.claimant.Claimant])(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      Logger.debug(s"saveClaimants in KeyStore: $claimants")
      cacheEntryForSession[List[_root_.models.claimant.Claimant]](claimants, claimantKey).map {
        result =>
          Logger.debug(s"saveClaimants: $result")
          result
      }
    }

    def loadHousehold()(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      fetchEntryForSession[_root_.models.household.Household](householdKey).map {
        result =>
          Logger.debug(s"loadHousehold: $result")
          result
      }
    }

    def saveHousehold(household : _root_.models.household.Household)(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      Logger.debug(s"saveHousehold in KeyStore: $household")
      cacheEntryForSession[_root_.models.household.Household](household, householdKey).map {
        result =>
          Logger.debug(s"saveHousehold: $result")
          result
      }
    }

  }

}
