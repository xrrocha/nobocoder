package nobocoder.scala.util

object FileSource {
  def lines(filename: String): Iterable[String] = io.Source.fromFile(filename).getLines.toIterable
}
