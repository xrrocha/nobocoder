package nobocoder.scala.spelling

import java.io.File
import scala.io.Source
import java.io.PrintWriter
import java.io.FileWriter
import org.apache.lucene.search.spell.LevensteinDistance

object Speller extends App {
  val WordPattern = "^[\\p{Alpha}]+$".r
  
  val dictionaryFilename = "../files/words.txt"
  val dictionaryFile = new File(dictionaryFilename)
  if (!dictionaryFile.exists()) {
    sys.error(s"Can't open $dictionaryFilename for reading")
  }
  val dictionary = Source.fromFile(dictionaryFile).getLines.toSet

  val ngram2wordFilename = "../files/ngram2word.txt"
  val ngram2wordFile = new File(ngram2wordFilename)
  val ngram2words: Map[String, Set[String]] =
    if (ngram2wordFile.exists()) {
      val ngramWords = for {
        line <- Source.fromFile(ngram2wordFile).getLines
        Array(ngram, wordList) = line.split("\\s+")
        words = wordList.split(",\\s*").toSet
      } yield (ngram, words)

      ngramWords.toMap
    } else {
      val ngramWords = for {
        word <- dictionary
        ngram <- ngramsFrom(word)
      } yield (ngram, word)

      val ngram2WordMap = ngramWords
        .groupBy { case (ngram, word) => ngram }
        .mapValues(_.map(_._2).toSet)

      val out = new PrintWriter(new FileWriter(ngram2wordFile), true)
      ngram2WordMap.foreach {
        case (ngram, words) =>
          out.println(s"$ngram\t${words.mkString(",")}")
      }

      ngram2WordMap
    }

  val minSimilarity = 0.725
  val stringDistance = new LevensteinDistance

  val wordSuggestions = for {
    word <- args
   
    normalizedWord = word.trim.toLowerCase.replaceAll("[^\\p{Alpha}]", "")
    if WordPattern.findFirstIn(normalizedWord).isDefined && !dictionary.contains(normalizedWord)

    val similarWords = for {
      ngram <- ngramsFrom(normalizedWord).toSeq
      similarWord <- ngram2words(ngram)
      similarityScore = stringDistance.getDistance(word, similarWord)
      if similarityScore >= minSimilarity
    } yield (similarWord, similarityScore)

    val orderedSimilarWords = similarWords
      .sortBy { case (similarWord, similarityScore) => -similarityScore }
      .map { case (similarWord, similarityScore) => similarWord }
  } yield (normalizedWord, orderedSimilarWords)

  val suggestionMap = wordSuggestions.toMap

  for ((word, suggestions) <- suggestionMap) {
    println(s"$word: [${suggestions.mkString(", ")}]")
  }

  def ngramsFrom(word: String, ngramLength: Int = 2): Set[String] = {
    for (pos <- 0 until word.length - ngramLength + 1)
      yield word.substring(pos, pos + ngramLength)
  }.toSet
}