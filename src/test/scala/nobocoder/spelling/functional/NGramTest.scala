package nobocoder.spelling.functional

import org.scalatest.FunSuite

class NGramBuilderTest extends FunSuite {

  val ngramBuilder = new NGramBuilder {
    override val ngramLength = 3
  }

  test("Builds ngrams for single word") {
    assert(ngramBuilder.ngrams("nobocoder") == Seq("nob", "obo", "boc", "oco", "cod", "ode", "der"))
  }

  test("Builds ngrams for multiple words") {
    assert(ngramBuilder.ngrams("nobocoder rocks") == Seq("nob", "obo", "boc", "oco", "cod", "ode", "der", "roc", "ock", "cks"))
  }

  test("Drops ngrams smaller than length") {
    assert(ngramBuilder.ngrams("no bo coder") == Seq("cod", "ode", "der"))
  }
}

class WordListNGramMapBuilderTest extends FunSuite {
  val ngramMapBuilder = new WordListNGramMapBuilder {
    val wordList = Seq("nobocoder", "is", "coders", "at", "work")
  }

  test("Builds ngram map") {
    val expectedNGramMap =
      Map(
        "boc" -> Seq("nobocoder"),
        "cod" -> Seq("coders", "nobocoder"),
        "der" -> Seq("coders", "nobocoder"),
        "ers" -> Seq("coders"),
        "nob" -> Seq("nobocoder"),
        "obo" -> Seq("nobocoder"),
        "oco" -> Seq("nobocoder"),
        "ode" -> Seq("coders", "nobocoder"),
        "ork" -> Seq("work"),
        "wor" -> Seq("work")
      )
    assert(ngramMapBuilder.buildNGramMap == expectedNGramMap)
  }
}

class LineNGramMapBuilderTest extends FunSuite {
  val ngramMapBuilder = new LineNGramMapBuilder {
    val lines = Seq(
      "boc\tnobocoder",
      "cod\tcoders,nobocoder",
      "der\tcoders,nobocoder",
      "ers\tcoders",
      "nob\tnobocoder",
      "obo\tnobocoder",
      "oco\tnobocoder",
      "ode\tcoders,nobocoder",
      "ork\twork",
      "wor\twork")
  }

  test("Builds ngram map") {
    val expectedNGramMap =
      Map(
        "boc" -> Seq("nobocoder"),
        "cod" -> Seq("coders", "nobocoder"),
        "der" -> Seq("coders", "nobocoder"),
        "ers" -> Seq("coders"),
        "nob" -> Seq("nobocoder"),
        "obo" -> Seq("nobocoder"),
        "oco" -> Seq("nobocoder"),
        "ode" -> Seq("coders", "nobocoder"),
        "ork" -> Seq("work"),
        "wor" -> Seq("work"))

    assert(ngramMapBuilder.buildNGramMap == expectedNGramMap)
  }
}


