package nobocoder.scala.spelling

trait SpellChecker {
  def suggestionsFor(word: String): Option[Seq[String]]

  def suggestionsFor(words: Seq[String]): Map[String, Seq[String]] =
    words.
      map(word => (word, suggestionsFor(word))).
      filter(_._2.isDefined).
      toMap.
      mapValues(_.get)
}

trait NGramSpellChecker extends SpellChecker { this: DictionaryBuilder with NGram2WordBuilder with StringDistance  =>
  import NGram._

  def minSimilarity: Double

  lazy val ngram2Word = buildNGram2Word
  lazy val dictionary = buildDictionary

  def suggestionsFor(word: String): Option[Seq[String]] =
    if (dictionary contains word) None
    else Some {
      ngrams(word, 2).
        filter(ngram2Word.contains).
        flatMap(ngram2Word).
        distinct.
        map { candidate =>
          (candidate, distance(candidate, word))
        }.
        filter(_._2 >= minSimilarity).
        sortBy(-_._2).
        map(_._1)
    }
}

