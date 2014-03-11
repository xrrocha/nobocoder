package nobocoder.spelling.functional

import org.scalatest.FunSuite
import org.apache.lucene.search.spell.JaroWinklerDistance

class SpellCheckerTest extends FunSuite {

  val spellChecker = new NGramSpellChecker with WordListNGramMapBuilder {
    lazy val wordList = Seq("nobocoder", "is", "coders", "at", "work")

    override val ngramLength = 2

    val minSimilarity = .84
    val jaro = {
      val distance = new JaroWinklerDistance
      distance.setThreshold(-1)
      distance
    }
    def scoreSimilarity(s1: String, s2: String) = jaro.getDistance(s1, s2)
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
