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

val suggestions = terms.
  filterNot(dictionary.contains).
  map { term =>
    val similars = dictionary.
      toSeq. // Convert dictionary `Set` to `Seq` so as to enable sorting
      map(word => (word, levenshtein.getDistance(term, word))). // Map each word to the tuple (word, similarity)
      filter(_._2 >= minSimilarity). // Equivalent to: filter{ case(term, similarity) => similarity >= minSimilarity }
      sortBy(-_._2). // Equivalent to: sortBy{ case(term, similarity) => -1 * similarity }
      map(_._1) // Equivalent to: map{ case(term, similarity) => term }

    (term, similars)
  }

suggestions foreach { case(term, similars) =>
  if (similars.isEmpty)
    println(s"Whaddaya mean '$term'?")
  else
    println(s"$term: you probably meant one of ${similars.mkString("(", ", ", ")")}")
}