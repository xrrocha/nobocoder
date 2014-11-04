val typo = "accet"

val relatedWords = Seq(
  "academic",
  "academy",
  "accent",
  "accept",
  "accident",
  "account",
  "accountant",
  "acid")

val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

relatedWords.foreach(w => println(f"$w ${levenshtein.getDistance(w, typo)}%.4f"))

