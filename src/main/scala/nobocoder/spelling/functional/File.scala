package nobocoder.spelling.functional

object FileSource {
  def lines(filename: String): Iterable[String] = io.Source.fromFile(filename).getLines.toIterable
}
