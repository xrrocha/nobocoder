package nobocoder.scala.spelling

import com.typesafe.scalalogging.StrictLogging
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import java.io.File
import nobocoder.scala.util.FileSource

trait SpellCheckerRunnerEnv {
  val wordFilename = "../files/words.txt"
  val ngram2wordFilename = "../files/ngram2word.txt"
}

object SpellCheckerRunner extends App with SpellCheckerRunnerEnv with StrictLogging {
  override val logger = Logger(LoggerFactory.getLogger(getClass))
  
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

    lazy val wordList = FileSource.lines(wordFilename)

    lazy val ngramLines = FileSource.lines(ngram2wordFilename)
  }

  val words: Seq[String] =
    if (args.nonEmpty)
      args
    else
      Seq("good", "word", "here",
          "badd", "wurd", "herre", "notaword")
  logger.debug(s"args: ${words.mkString(" ")}")

  spellChecker.suggestionsFor(words).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}

object SpellCheckerPreparer extends App with SpellCheckerRunnerEnv {
  prepareNGram2WordFile()

  def prepareNGram2WordFile() {
    val builder = new WordListNGram2WordBuilder {
      val ngramWordList = FileSource.lines(wordFilename)
    }

    val ngramMap = builder.buildNGram2Word

    LineNGram2WordBuilder.save(ngramMap, ngram2wordFilename)
  }
}
