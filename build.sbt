ThisBuild / scalaVersion := "3.3.7"
ThisBuild / majorVersion := 1

lazy val microservice = Project("trusts-individual-check-stub", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 9847,
    libraryDependencies ++= AppDependencies(),
    CodeCoverageSettings(),
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    )
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
