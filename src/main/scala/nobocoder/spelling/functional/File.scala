package nobocoder.spelling.functional

trait FileSource {
  def filename: String

  def lines: Iterable[String] = io.Source.fromFile(filename).getLines.toIterable
}
