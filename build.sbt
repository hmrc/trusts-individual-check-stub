import scoverage.ScoverageKeys.*

val appName = "trusts-individual-check-stub"

lazy val scoverageSettings =
  Seq(
    coverageExcludedPackages := "<empty>;.*config.*;Reverse.*;.*BuildInfo.*;.*Routes.*;.*GuiceInjector;",
    coverageMinimumStmtTotal := 90,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    PlayKeys.playDefaultPort         := 9847,
    scalaVersion                     := "2.13.11",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings,
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
  // Try to remove when sbt[ 1.8.0+ and scoverage is 2.0.7+
  .settings(libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always))
