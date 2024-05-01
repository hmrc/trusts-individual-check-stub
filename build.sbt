import scoverage.ScoverageKeys.*

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

val appName = "trusts-individual-check-stub"

lazy val scoverageSettings =
  Seq(
    coverageExcludedPackages := "<empty>;.*Routes.*;",
    coverageMinimumStmtTotal := 92,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort         := 9847,
    libraryDependencies              ++= AppDependencies(),
    scoverageSettings,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    )
  )

addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
