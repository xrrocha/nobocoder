package nobocoder.spelling.functional


trait NGramBuilder {
  def ngramLength: Int = 4

  def ngrams(string: String): Seq[String] =
    string.
      split(' ').
      filter(_.length >= ngramLength).
      flatMap(_.sliding(ngramLength).filter(_.size == ngramLength)).
      distinct
}

trait NGram2WordBuilder extends NGramBuilder {
  def buildNGram2WordMap: Map[String, Seq[String]]
}

trait WordListNGram2WordBuilder extends NGram2WordBuilder {
  def words: Iterable[String]

  def buildNGram2WordMap: Map[String, Seq[String]] =
    words.
      flatMap { word =>
      ngrams(word).map(ngram => (ngram, word))
    }.
      groupBy(_._1).
      mapValues(_.map(_._2).toSeq)
}

trait LineNGram2WordBuilder extends NGram2WordBuilder {
  val LineRegex = """^([^\t]+)\t(.*)""".r

  def lines: Iterable[String]

  def buildNGram2WordMap: Map[String, Seq[String]] =
    lines
      .map { line =>
      val LineRegex(ngram, words) = line
      ngram -> words.split(",").toSeq
    }.
      toMap
}
