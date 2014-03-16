package nobocoder.spelling.scripting

import io.Source

object URLFetcher extends App {
  val DefaultURL = "http://scala-lang.org"
  val url = if (args.length == 0) DefaultURL else args(0)

  print(Source.fromURL(url).mkString)
}

