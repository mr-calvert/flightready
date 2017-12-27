lazy val commonOptions = Seq(
  version := "1.0",
  scalaVersion := "2.12.4",
  scalacOptions += "-Ypartial-unification"
)

lazy val core = (project in file("core"))
    .settings(commonOptions)

lazy val javaio = (project in file("javaio"))
    .settings(commonOptions)
    .dependsOn(core)
