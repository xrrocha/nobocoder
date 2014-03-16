// Load dictionary from file
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet

// Build the similarity scorer
val minSimilarity = 0.75
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

// Set the terms being examined
val terms = Seq("good", "word", "here", "badd", "wurd", "herre", "notaword")

//terms foreach { term =>
//  if (!(dictionary contains term))
//    dictionary foreach { knownWord =>
//      if (levenshtein.getDistance(term, knownWord) >= minSimilarity)
//        println(s"$term: did you mean $knownWord?")
//    }
//}

terms.
  filterNot(dictionary.contains).
  foreach { term =>
    val similars = dictionary filter { word =>
      levenshtein.getDistance(term, word) >= minSimilarity
    }
    if (similars.isEmpty)
      println(s"Whaddaya mean '$term'?")
    else
      println(s"$term: you probably meant one of $similars")
  }