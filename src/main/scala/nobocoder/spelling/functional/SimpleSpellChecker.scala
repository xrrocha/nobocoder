package nobocoder.spelling.functional

import org.apache.lucene.search.spell.{JaroWinklerDistance, StringDistance}

class SimpleSpellChecker(
    val wordList: Iterable[String],
    val stringDistance: StringDistance,
    val minSimilarity: Double,
    override val ngramLength: Int = 2)
  extends NGramSpellChecker with WordListNGramMapBuilder
{
  def scoreSimilarity(s1: String, s2: String) = stringDistance.getDistance(s1, s2)
}

object SimpleSpellChecker extends App {
  val spellChecker = new SimpleSpellChecker(
    wordList       = io.Source.fromFile("files/words.txt").getLines.toIterable,
    minSimilarity  = .845,
    ngramLength    = 2,
    stringDistance = {
      val distance = new JaroWinklerDistance
      distance.setThreshold(-1)
      distance
    }
  )

  spellChecker.suggestionsFor(args).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}
