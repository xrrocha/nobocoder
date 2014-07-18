version := "1.0"

organization := "nobocoder"

scalaVersion := "2.11.1"

scalacOptions ++= Seq(
	"-deprecation",
	"-feature",
	"-language:implicitConversions",
	"-language:postfixOps",
	"-language:experimental.macros")

//retrieveManaged := true

name := "spellchecker"

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-spellchecker" % "3.6.2",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "junit" % "junit" % "4.11" % "test"
)

