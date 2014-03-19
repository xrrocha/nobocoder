package nobocoder.spelling.functional

import com.typesafe.scalalogging.slf4j.Logging
import java.io.File

trait SpellCheckerRunnerEnv {
  val wordFilename = "files/words.txt"
  val ngram2wordFilename = "files/ngram2word.txt"
}

object SpellCheckerPreparer extends App with SpellCheckerRunnerEnv {
  prepareNGram2WordFile()

  def prepareNGram2WordFile() {
    val builder = new WordListNGram2WordBuilder {
      val wordList = FileSource.lines(wordFilename)
    }

    val ngramMap = builder.buildNGram2Word

    LineNGram2WordBuilder.save(ngramMap, ngram2wordFilename)
  }
}

object SpellCheckerRunner extends App with SpellCheckerRunnerEnv with Logging {
  if (!new File(ngram2wordFilename).exists()) {
    logger.info(s"Preparing ngram2word file")
    SpellCheckerPreparer.prepareNGram2WordFile()
  }

  val spellChecker = new NGramSpellChecker
    with WordListDictionaryBuilder
    with LineNGram2WordBuilder
    with LuceneStringDistance
  {
    val minSimilarity = 0.75
    val stringDistance = new org.apache.lucene.search.spell.LevensteinDistance

    lazy val wordLines = FileSource.lines(wordFilename)

    lazy val ngramLines = FileSource.lines(ngram2wordFilename)
  }

  logger.debug(s"args: ${args.mkString(" ")}")

  spellChecker.suggestionsFor(args).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}
