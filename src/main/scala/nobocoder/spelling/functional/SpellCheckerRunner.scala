package nobocoder.spelling.functional

import com.typesafe.scalalogging.slf4j.Logging
import java.io.File

object SpellCheckerRunner extends App with Logging {
  val wordFilename = "files/words.txt"
  val words = FileSource.lines(wordFilename)

  val ngram2wordFilename = "files/ngram2word.txt"
  val ngram2words = if (new File(ngram2wordFilename).exists()) {
    val builder = new LineNGram2WordBuilder {
      val ngramLines = FileSource.lines(ngram2wordFilename)
    }

    builder.buildNGram2Word
  } else {
    val builder = new WordListNGram2WordBuilder {
      val wordList = words
    }
    val ngramMap = builder.buildNGram2Word
    LineNGram2WordBuilder.save(ngramMap, ngram2wordFilename)
    ngramMap
  }

  val spellChecker = new NGramSpellChecker
    with WordListDictionaryBuilder
    with NGram2WordBuilder
    with LuceneStringDistance
  {
    val minSimilarity = 0.75
    val stringDistance = new org.apache.lucene.search.spell.LevensteinDistance

    lazy val wordLines = SpellCheckerRunner.words

    override lazy val buildNGram2Word = SpellCheckerRunner.ngram2words
  }

  logger.debug(s"args: ${args.mkString(" ")}")

  spellChecker.suggestionsFor(args).
    foreach { case (word, suggestions) =>
      println(s"$word: ${suggestions.mkString(",")}")
    }
}
