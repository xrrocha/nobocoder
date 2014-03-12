package nobocoder.spelling.functional

import org.apache.lucene.search.spell.{JaroWinklerDistance, StringDistance}
import com.typesafe.scalalogging.slf4j.Logging

class SimpleSpellChecker(
    val wordList: Iterable[String],
    val stringDistance: StringDistance,
    val minSimilarity: Double,
    override val ngramLength: Int = 2)
  extends NGramSpellChecker with WordListNGramMapBuilder
{
  def scoreSimilarity(s1: String, s2: String) = stringDistance.getDistance(s1, s2)
}

object SpellCheckerRunner extends App with Logging {
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

  logger.debug(s"args: ${args.mkString(" ")}")
  spellChecker.suggestionsFor(args).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}
