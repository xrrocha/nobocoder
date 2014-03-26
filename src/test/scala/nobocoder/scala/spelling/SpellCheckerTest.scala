package nobocoder.scala.spelling

import org.scalatest.FunSuite
import org.apache.lucene.search.spell.JaroWinklerDistance

class NGramSpellCheckerTest extends FunSuite {

  val spellChecker = new NGramSpellChecker
    with WordListDictionaryBuilder
    with WordListNGram2WordBuilder
    with LuceneStringDistance
  {
    lazy val ngramWordList = Seq("nobocoder", "is", "coders", "at", "work")
    lazy val wordList = ngramWordList

    val minSimilarity = .84
    val stringDistance = {
      val distance = new JaroWinklerDistance
      distance.setThreshold(-1)
      distance
    }

    println(buildNGram2Word)
  }

  test("Suggests nothing for existing words") {
    assert(spellChecker.suggestionsFor("nobocoder") isEmpty)
    assert(spellChecker.suggestionsFor("work") isEmpty)
  }

  test("Suggests appropriate similar words") {
    assert(spellChecker.suggestionsFor("novocoder") == Some(Seq("nobocoder")))
    assert(spellChecker.suggestionsFor("codders") == Some(Seq("coders")))
    assert(spellChecker.suggestionsFor("werk") == Some(Seq("work")))
  }
}
