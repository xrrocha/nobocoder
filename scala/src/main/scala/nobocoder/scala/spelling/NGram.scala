package nobocoder.scala.spelling

import java.io.{FileWriter, PrintWriter}

object NGram {
  def ngrams(string: String, ngramLength: Int): Seq[String] =
    string.
      trim.
      toLowerCase.
      split("\\s+").
      filter(_.length >= ngramLength).
      flatMap(_.sliding(ngramLength).filter(_.size == ngramLength)).
      distinct
}

trait NGram2WordBuilder {
  def buildNGram2Word: Map[String, Seq[String]]
}

trait WordListNGram2WordBuilder extends NGram2WordBuilder {
  val ngramLength = 2
  def ngramWordList: Iterable[String]

  import NGram._

  def buildNGram2Word: Map[String, Seq[String]] =
    ngramWordList.
      flatMap { word =>
        ngrams(word, ngramLength).map(ngram => (ngram, word))
      }.
      groupBy(_._1).
      mapValues(_.map(_._2).toSeq.sorted)
}

trait LineNGram2WordBuilder extends NGram2WordBuilder {
  def ngramLines: Iterable[String]

  def buildNGram2Word: Map[String, Seq[String]] =
    ngramLines
      .map { line =>
        val Array(ngram, words) = line.split("\t")
        ngram -> words.split(",").toSeq
      }.
      toMap
}

object LineNGram2WordBuilder {
  def save(ngram2word: Map[String, Seq[String]], filename: String) {
    val out = new PrintWriter(new FileWriter(filename), true)
    ngram2word.foreach { case(ngram, words) =>
      out.println(s"$ngram\t${words.mkString(",")}")
    }
    out.close()
    out.flush()
  }
}
