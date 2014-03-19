package nobocoder.spelling.functional

trait DictionaryBuilder {
  def buildDictionary: Set[String]
}

trait WordListDictionaryBuilder extends DictionaryBuilder {
  def wordLines: Iterable[String]

  def buildDictionary: Set[String] =
    wordLines.
      flatMap { word =>
        word.split("\\s+").map(_.trim.toLowerCase)
      }.
      toSet
}
