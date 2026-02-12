import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-backend-play-30" % bootstrapVersion,
    "com.github.java-json-tools" % "json-schema-validator"      % "2.2.14"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
