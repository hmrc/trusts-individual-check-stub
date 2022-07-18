import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28" % "6.3.0",
    "com.github.fge"          %  "json-schema-validator"     % "2.2.6"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.2.12"   % Test,
    "com.typesafe.play"       %% "play-test"                % current   % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.62.2" % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"   % "test, it"
  )
}
