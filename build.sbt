lazy val commonOptions = Seq(
  version := "1.0",
  scalaVersion := "2.12.4",
  scalacOptions += "-Ypartial-unification"
)

lazy val core =
  (project in file("core"))
    .settings(commonOptions)

lazy val catsIntegration =
  (project in file("cats-integration"))
    .dependsOn(core)
    .settings(
      commonOptions,
      libraryDependencies += "org.typelevel" %% "cats-effect" % "0.5"
    )


lazy val javaio =
  (project in file("javaio"))
    .dependsOn(core)
    .settings(commonOptions)

lazy val javaioCatsExamples =
  (project in file("javaio-cats-examples"))
    .dependsOn(javaio, catsIntegration)
    .settings(commonOptions)
