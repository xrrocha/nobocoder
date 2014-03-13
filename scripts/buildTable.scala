val dictionary = Seq(
  "academic",
  "academy",
  "accent",
  "accept",
  "accident",
  "account",
  "accountant",
  "acid",
  "count")

val typo = "academmic"

import org.apache.lucene.search.spell._
val metrics = Seq(new LevensteinDistance, new JaroWinklerDistance)

val similarities =
  dictionary.map { word =>
    (word, metrics.map(_.getDistance(typo, word)))
  }.
    sortBy(-_._2.product)

val header =
  """
    ||Word|Levenshtein|JaroWinkler|
    ||----|----------:|----------:|
    |""".stripMargin

val body =
  similarities.map { case(word, scores) =>
    s"|$word|${scores.map(score => f"$score%.4f").mkString("|")}|"
  }.
    mkString("\n")


println(s"$header$body")
// Prints:
/*
|Word|Levenshtein|JaroWinkler|
|----|----------:|----------:|
|academic|0.8889|0.9852|
|academy|0.6667|0.9365|
|acid|0.3333|0.6944|
|accident|0.3333|0.6481|
|accent|0.3333|0.6111|
|accept|0.3333|0.6111|
|account|0.2222|0.5026|
|accountant|0.2000|0.4741|
|count|0.1111|0.4370|
*/