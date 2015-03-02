package nobocoder.snippets

import org.apache.lucene.search.spell.LevensteinDistance

object S01_SuggestionsForTypo extends App {
  
  // Similarity metric
  val minSimilarity = 0.75
  val levenshtein = new LevensteinDistance
  
  // The typo in question
  val typo = "accet"
  
  // Extract bigrams from typo
  val ngramLength = 2
  val typoNgrams = {
    for (pos <- 0 until typo.length - ngramLength + 1)
      yield typo.substring(pos, pos + ngramLength) 
  }.toSet // Force collection to set so as to remove dups
  
  val suggestions = typoNgrams
          // Select only ngrams appearing in the ngram-to-word map
          .filter(ngram2Words.contains)
          // Produce the set of all words sharing ngrams with typo
          .flatMap(ngram => ngram2Words(ngram))
          // Convert each word into a word/similarity score pair
          .map(word => (word, levenshtein.getDistance(word, typo)))
          // Remove scores below minimum allowed similarity
          .filter(_._2 >= minSimilarity)
          // Make into sequence for sorting
          .toSeq
          // Sort descending so as to show more similar words first
          .sortBy(-_._2)
          // Extract word only, removing score
          .map(_._1)
  
  // Prints: Suggestions for 'accet': [accent, accept]
  println(s"Suggestions for '$typo': [${suggestions.mkString(", ")}]")
  
  lazy val ngram2Words = Map(
    "ac" -> Set("academic", "academy", "accent", "accept",
                "accident", "account", "accountant", "acid"),
    "ad" -> Set("academic", "academy"),
    "an" -> Set("accountant"),
    "ca" -> Set("academic", "academy"),
    "cc" -> Set("accent", "accept", "accident",
                "account", "accountant"),
    "ce" -> Set("accent", "accept"),
    "ci" -> Set("accident", "acid"),
    "co" -> Set("account", "accountant", "count"),
    "de" -> Set("academic", "academy", "accident"),
    "em" -> Set("academic", "academy"),
    "en" -> Set("accent", "accident"),
    "ep" -> Set("accept"),
    "ic" -> Set("academic"),
    "id" -> Set("accident", "acid"),
    "mi" -> Set("academic"),
    "my" -> Set("academy"),
    "nt" -> Set("accent", "accident", "account",
                "accountant", "count"),
    "pt" -> Set("accept"),
    "ta" -> Set("accountant"),
    "un" -> Set("account", "accountant", "count")
  )
}
