package nobocoder.spelling.functional


trait NGramBuilder {
  def ngramLength: Int = 3

  def ngrams(string: String): Seq[String] =
    string.
      split(' ').
      filter(_.length >= ngramLength).
      flatMap(_.sliding(ngramLength).filter(_.size == ngramLength)).
      distinct
}

object NGramMapBuilder {
  type NGramMap = Map[String, Seq[String]]
}

import NGramMapBuilder._

trait NGramMapBuilder extends NGramBuilder {
  def buildNGramMap: NGramMap
}

trait WordListNGramMapBuilder extends NGramMapBuilder {
  def wordList: Iterable[String]

  def buildNGramMap: NGramMap =
    wordList.
      flatMap { word =>
        ngrams(word).map(ngram => (ngram, word))
      }.
      groupBy(_._1).
      mapValues(_.map(_._2).toSeq.sorted)
}

trait LineNGramMapBuilder extends NGramMapBuilder {
  val LineRegex = """^([^\t]+)\t(.*)""".r

  def lines: Iterable[String]

  def buildNGramMap: NGramMap =
    lines
      .map { line =>
        val LineRegex(ngram, words) = line
        ngram -> words.split(",").toSeq
      }.
      toMap
}
