package nobocoder.spelling

trait StringDistance {
  def distance(s1: String, s2: String): Double
}

trait LuceneStringDistance extends StringDistance {
  def stringDistance: org.apache.lucene.search.spell.StringDistance

  def distance(s1: String, s2: String) = stringDistance.getDistance(s1, s2)
}


