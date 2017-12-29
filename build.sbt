lazy val commonOptions = Seq(
  version := "1.0",
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-Ypartial-unification",
    "-deprecation",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xlint:delayedinit-select",
    "-Xlint:doc-detached",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload",
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard"
  )
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
