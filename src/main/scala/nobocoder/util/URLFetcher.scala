package nobocoder.util

import io.Source

object URLFetcher extends App with Timer with Logger {
  val DefaultURL = "http://scala-lang.org"
  val url = if (args.length == 0) DefaultURL else args(0)

  val (contents, elapsedTime) = time(Source.fromURL(url).mkString)
  print(contents)

  log(s"Elapsed time: $elapsedTime milliseconds")
}

trait Timer {
  def time[A](action: => A) = {
    val startTime = System.currentTimeMillis()
    val result = action
    val endTime = System.currentTimeMillis()
    (result, endTime - startTime)
  }
}

trait Logger {
  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

  def log(message: String) {
    System.err.println(s"[${dateFormat.format(new java.util.Date)}] $message")
  }
}

