package nobocoder.spelling.functional

trait SpellChecker {
  def suggestionsFor(word: String): Option[Seq[String]]

  def suggestionsFor(words: Seq[String]): Map[String, Seq[String]] =
    words.
      map(word => (word, suggestionsFor(word))).
      filter(_._2.isDefined).
      toMap.
      mapValues(_.get)
}

trait NGramSpellChecker extends  SpellChecker with NGramBuilder {
  def ngram2Word: Map[String, Seq[String]]

  def minSimilarity: Double
  def scoreSimilarity(s1: String, s2: String): Double

  val words: Set[String] = ngram2Word.values.flatten.toSet

  def suggestionsFor(word: String): Option[Seq[String]] =
    if (words contains word) None
    else Some {
      ngrams(word).
        flatMap(ngram2Word).
        distinct.
        map { candidate =>
          (candidate, scoreSimilarity(candidate, word))
        }.
        filter(_._2 >= minSimilarity).
        sortBy(-_._2).
        map(_._1)
    }
}

import org.apache.lucene.search.spell.StringDistance
trait LuceneNGramSpellChecker extends NGramSpellChecker {
  def stringDistance: StringDistance
  def scoreSimilarity(s1: String, s2: String): Double = stringDistance.getDistance(s1, s2)
}

