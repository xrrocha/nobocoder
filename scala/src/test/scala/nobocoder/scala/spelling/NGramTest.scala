package nobocoder.scala.spelling

import org.scalatest.FunSuite

class NGramTest extends FunSuite {
  test("Builds ngrams for single word") {
    assert(NGram.ngrams("nobocoder", 3) == Seq("nob", "obo", "boc", "oco", "cod", "ode", "der"))
  }

  test("Builds ngrams for multiple words") {
    assert(NGram.ngrams("nobocoder rocks", 3) == Seq("nob", "obo", "boc", "oco", "cod", "ode", "der", "roc", "ock", "cks"))
  }

  test("Drops ngrams smaller than length") {
    assert(NGram.ngrams("no bo coder", 3) == Seq("cod", "ode", "der"))
  }
}

class WordListNGramMapBuilderTest extends FunSuite {
  val ngramMapBuilder = new WordListNGram2WordBuilder {
    override val ngramLength = 3
    val ngramWordList = Seq("nobocoder", "is", "coders", "at", "work")
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
    assert(ngramMapBuilder.buildNGram2Word == expectedNGramMap)
  }
}

class LineNGramMapBuilderTest extends FunSuite {
  val ngramMapBuilder = new LineNGram2WordBuilder {
    val ngramLines = Seq(
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

    assert(ngramMapBuilder.buildNGram2Word == expectedNGramMap)
  }
}


