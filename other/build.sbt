name := "nobocoder"

version := "0.1"

scalaVersion := "2.10.3"

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-spellchecker" % "3.6.2",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5" ,//% "provided",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)
