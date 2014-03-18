package nobocoder.spelling.functional

import com.typesafe.scalalogging.slf4j.Logging

object SpellCheckerRunner extends App with Logging {
  val spellChecker = new NGramSpellChecker
    with WordFileDictionaryBuilder
    with WordListNGram2WordBuilder
    with LuceneStringDistance
  {
    val minSimilarity = 0.75
    val stringDistance = new org.apache.lucene.search.spell.LevensteinDistance

    lazy val wordList = dictionary
    val filename = "files/words.txt"
  }

  logger.debug(s"args: ${args.mkString(" ")}")

  spellChecker.suggestionsFor(args).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}
