// Load dictionary from file
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet

// Build the similarity scorer
val minSimilarity = 0.75
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

// Set the terms being examined
val terms = Seq("good", "word", "here", "badd", "wurd", "herre", "notaword")

// Collect suggestions
//val suggestions = terms.
//  filterNot(dictionary.contains).
//  map { term =>
//    val similars = dictionary.
//      toSeq. // Convert dictionary `Set` to `Seq` so as to enable sorting
//      map(word => (word, levenshtein.getDistance(term, word))). // Map each word to the tuple (word, similarity)
//      filter(_._2 >= minSimilarity). // Equivalent to: filter{ case(term, similarity) => similarity >= minSimilarity }
//      sortBy(-_._2). // Equivalent to: sortBy{ case(term, similarity) => -1 * similarity }
//      map(_._1) // Equivalent to: map{ case(term, similarity) => term }
//
//    (term, similars)
//  }

val suggestions = for {
  term <- terms
  if !(dictionary contains term)
  similars = {
    val wordSimilarities = for {
      word <- dictionary.toSeq
      similarity = levenshtein.getDistance(term, word)
      if similarity >= minSimilarity
    } yield (word, similarity)
    for {
      (word, similarity) <- wordSimilarities.sortBy(-_._2)
    } yield word
  }
} yield (term, similars)

// Output suggestions
suggestions foreach { case(term, similars) =>
  if (similars.isEmpty)
    println(s"Whaddaya mean '$term'?")
  else
    println(s"$term: you probably meant one of ${similars.mkString("(", ", ", ")")}")
}