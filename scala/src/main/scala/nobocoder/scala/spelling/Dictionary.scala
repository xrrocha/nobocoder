package nobocoder.scala.spelling

trait DictionaryBuilder {
  def buildDictionary: Set[String]
}

trait WordListDictionaryBuilder extends DictionaryBuilder {
  def wordList: Iterable[String]

  def buildDictionary: Set[String] =
    wordList
      .flatMap { word =>
        word.split("\\s+").map(_.trim.toLowerCase)
      }
      .toSet
}
