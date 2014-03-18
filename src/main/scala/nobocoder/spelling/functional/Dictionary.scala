package nobocoder.spelling.functional

trait DictionaryBuilder {
  def buildDictionary: Set[String]
}

trait WordListDictionaryBuilder extends DictionaryBuilder {
  def lines: Iterable[String]

  def buildDictionary: Set[String] =
    lines.
      flatMap { word =>
      word.split("\\s+").map(_.trim.toLowerCase)
    }.
      toSet
}

trait WordFileDictionaryBuilder extends WordListDictionaryBuilder with FileSource