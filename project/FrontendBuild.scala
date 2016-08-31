import sbt._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import play.PlayImport.PlayKeys._
import sbt.Keys._
import scala.util.Properties.envOrElse

object FrontendBuild extends Build with MicroService {

  val appName = "cc-frontend"
 
  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  // play settings
  override lazy val playSettings : Seq[Setting[_]] = Seq(
    routesImport ++= Seq("uk.gov.hmrc.domain._"),
    // Turn off play's internal less compiler
    lessEntryPoints := Nil,
    // Turn off play's internal javascript compiler
    javascriptEntryPoints := Nil,
    // Add the views to the dist
    unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
    // Dont include the source assets in the dist package (public folder)
    excludeFilter in Assets := "sass*" || "img*"
  ) ++ JavaScriptBuild.javaScriptUiSettings
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playConditionalMappingVersion = "0.2.0"
  private val playHealthVersion = "1.1.0"
  private val govUkTemplateVersion = "4.0.0"
  private val httpCachingClientVersion ="5.3.0"
  private val playUiVersion = "4.9.0"
  private val urlBuilderVersion = "1.0.0"
  private val frontendBootStrap = "6.4.0"
  private val playPartials = "4.2.0"
  private val playConfig = "2.0.1"
  private val playJsonLogger = "2.1.1"
  private val playAuthorisedFrontend = "4.6.0"
  private val httpVerbs = "3.3.0"
  private val emailAddress = "1.1.0"

  private val scalaTestVersion = "2.2.2"
  private val jSoupVersion = "1.7.3"
  private val mockitoVersion = "1.10.19"
  private val hmrcTestVersion = "1.4.0"
  private val pegDownVersion = "1.4.2"

  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalMappingVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "govuk-template" % govUkTemplateVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddress,
    "uk.gov.hmrc" %% "url-builder" % urlBuilderVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddress,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootStrap, // includes the global object and error handling, as well as the FrontendController classes and some common configuration
    "uk.gov.hmrc" %% "play-partials" % playPartials, // includes code for retrieving partials, e.g. the Help with this page form
    "uk.gov.hmrc" %% "play-config" % playConfig, // includes helper classes for retrieving Play configuration
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLogger, // required for the JSON logger
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontend, // use when your frontend requires authentication
    "uk.gov.hmrc" %% "http-verbs" % httpVerbs
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoVersion % scope,
        "org.jsoup" % "jsoup" % jSoupVersion % scope,
        "org.pegdown" % "pegdown" % pegDownVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


