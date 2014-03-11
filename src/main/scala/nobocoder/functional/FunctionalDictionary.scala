package nobocoder.functional

object NGram {
  def ngrams(string: String, length: Int = 4): Iterable[String] =
    string.
      split(' ').
      filter(_.length >= length).
      flatMap(_.sliding(length).filter(_.size == length)).
      distinct

  def ngramPairs(elements: Seq[String], length: Int = 4): Seq[(Int, Int)] =
    elements.
      par.
      zipWithIndex.
      flatMap { case (element, index) =>
        ngrams(element, length).map(ngram => (ngram, index))
      }.
      groupBy(_._1).
      values.
      map(_.map(_._2).toSeq.distinct.seq.sorted).
      flatMap { seq =>
        for {
          left <- 0 until seq.size
          right <- left + 1 until seq.size
        } yield (seq(left), seq(right))
      }.
      toVector.
      distinct.
      seq
}

trait NGramMapBuilder {
  def minSimilarity: Double
  def compare(word1: String, word2: String): Double

  def builNGramMap(words: Seq[String], ngramLength: Int = 4) =
    words.
      par.
      zipWithIndex.
      flatMap { case (word, index) =>
        NGram.ngrams(word, ngramLength).map(ngram => ngram -> words(index))
      }.
      groupBy(_._1).
      mapValues(_.map(_._2).seq.sorted).
      seq
}

trait SimilarityDecider[A] {
  def similar(a1: A, a2: A): Boolean
}

trait ComparingStringSimilarityDecider extends SimilarityDecider[String] {
  def minSimilarity: Double
  def compare(a1: String, a2: String): Double

  def similar(a1: String, a2: String): Boolean = compare(a1, a2) >= minSimilarity
}

trait NGramMap { this: SimilarityDecider[String] =>
  def ngramLength: Int
  def ngramMap: Map[String, Seq[String]]

  def contains(word: String) = ngramMap.contains(word)

  def similarWords(word: String) =
    NGram.ngrams(word, ngramLength).
      flatMap(ngramMap).
      toSeq.
      distinct.
      filter(similar(word, _))
}
