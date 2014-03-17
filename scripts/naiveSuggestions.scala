// Load dictionary from file
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet

// Build the similarity scorer
val minSimilarity = 0.75
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

// Set the terms being examined
val terms = Seq("good", "word", "here", "badd", "wurd", "herre", "notaword")

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
for ((term, similars) <- suggestions) {
  if (similars.isEmpty)
    println(s"Whaddaya mean '$term'?")
  else
    println(s"$term: you probably meant one of ${similars.mkString("(", ", ", ")")}")
}
