name := "nobocoder"

version := "0.1"

scalaVersion := "2.11.3"

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-spellchecker" % "3.6.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7" ,
  "org.scalatest" %% "scalatest" % "2.2.2" % "test"
)