name := "nobocoder-book"

version := "1.0"

scalaVersion := "2.11.5"

scalatex.SbtPlugin.projectSettings

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "scalatags" % "0.4.5",
  "com.lihaoyi" %% "scalatex-site" % "0.1.1",
  "com.lihaoyi" %% "ammonite" % "0.1.0"
)
    