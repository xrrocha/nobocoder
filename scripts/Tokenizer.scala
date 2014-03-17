import com.typesafe.scalalogging.slf4j.Logging

trait Tokenizer {
  def tokenize(string: String): Seq[String]
}

class DefaultTokenizer(separator: String) extends Tokenizer {
  def tokenize(string: String) = string.split(separator)
}

object Tokenizer extends Logging {
  def apply(): Tokenizer = {
    logger.debug("Using default separator")
    apply("\\s")
  }
  def apply(separator: String): Tokenizer = new DefaultTokenizer(separator)
}

val tokenizer = Tokenizer(",")
println(tokenizer.tokenize("a,b,c")) // prints: WrappedArray(a, b, c)