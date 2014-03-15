// Load dictionary from file
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet

// Build the similarity scorer
val minSimilarity = 0.75
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

// Set the terms being examined
val terms = Seq("good", "word", "here", "badd", "wurd", "herre")

terms foreach { term =>
  if (!(dictionary contains term))
    dictionary foreach { knownWord =>
      if (levenshtein.getDistance(term, knownWord) >= minSimilarity)
        println(s"$term: did you mean $knownWord?")
    }
}