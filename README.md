# North Boynton Coders Scala Presentation #

## Simple Spelling Suggestion in Scala ##

This document describes a (rather simplistic) spelling suggestion algorithm
and its evolving implementation in Scala. This implementation starts as an
imperative solution -not unlike what one would write in, say, Java- and is
subsequently modified towards a more functional, Scala-idiomatic style.

## Spelling Checking in a Nutshell ##

In its simplest form, spelling checking is carried out by looking up words
in a dictionary.

Words found in the dictionary are deemed to be correct and, therefore, don't
need any suggestion.

Words not found in the dictionary, on the other hand, can be either:

- Legitimate new terms that should be added to the dictionary, or
- Misspellings for which a list of similar words need be provided

## String Similarity ##

The key word above is _similar_: how do we decide whether two words are similar
enough so as to recommend one as a possible replacement for the other?

_String similarity_ algorithms quantify the similarity between pairs of strings.
Customarily, similarity values oscillate between `0` (not similar at all) to `1`
(exactly equal.) String similarity metrics are also referred to as _string distance_
metrics.

There's no shortage of [string similarity metrics](http://en.wikipedia.org/wiki/String_similarity)
but for our purposes we'll play with two popular ones: Levenshtein and JaroWinkler.

The [Levenshtein distance](http://en.wikipedia.org/wiki/Levenshtein_distance) measures the number of characters _edits_ (deletions, transpositions, additions) required to turn one string into another. The higher
the number of edits, the less similar the two strings are. Zero edits means the
strings are one and the the same. Given the maximum length of the two strings, the
number of edits can be expressed as a value between `0` and `1`.

The [Jaro-Winkler distance](http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance) metric measures character commonality between two strings favoring
those with a common prefix. This algorithm always yields a number between `0` and `1`
and is typically used to compare person names.

For our implementation we'll use Apache Lucene's
[string distance support](http://lucene.apache.org/core/3_5_0/api/contrib-spellchecker/org/apache/lucene/search/spell/StringDistance.html).

## Similarity Examples ##

Consider the following dictionary fragment:

|Word|
|----|
|academic|
|academy|
|accent|
|accept|
|accident|
|account|
|accountant|
|acid|
|count|

The following table shows the similarity scores for the above dictionary words and the misspelled word _academmic_:

|Word|Levenshtein|JaroWinkler|
|----|----------:|----------:|
|academic|0.8889|0.9852|
|academy|0.6667|0.9365|
|acid|0.3333|0.6944|
|accident|0.3333|0.6481|
|accent|0.3333|0.6111|
|accept|0.3333|0.6111|
|account|0.2222|0.5026|
|accountant|0.2000|0.4741|
|count|0.1111|0.4370|

> Note: the above table's markdown was generated with a
> [Scala script](scripts/buildTable.scala).

As we can see, we need to establish a _minimum similarity_ threshold for each metric
in order to weed out not-so-similar terms. In the above example, it would appear that
anything below `0.7` for the Levenshtein metric is probably not similar enough.

## Comparison Explosion ##

In order to be exhaustive, a naïve spelling suggestion implementation would compare
each unknown word with _all_ words in the dictionary collecting only those whose
similarity score is above a configured threshold.

A useful dictionary will contain several tens of thousand words. Comparing each
unknown term with so many words is clearly unacceptable, especially given how costly
a similarity comparison is as opposed to simple string equality

Thus, for a 72k-word dictionary and a Levenshtein threshold of `0.725`, the following
3 typos would require 216,000 similarity comparisons to come up with the few suggestions shown below:

|Typo|Suggestions|
|----|-----------|
|acident|accident, accidents, acridest, incident, occident|
|academmy|academy, academia, academic|
|accountn|account, accounts, accountant, accounting, accounted, accountancy, accountants|

## N-Grams to the Rescue ##

An inexpensive mechanism is needed to avoid performing costly comparisons most of
which will yield scores below the minimum similarity threshold.

As it turns out, string similarity is closely related to character commonality:
the higher a similarity score is for a given pair of string the more characters
they share at the same or very close positions.

An [_n-gram_](http://en.wikipedia.org/wiki/N-gram) is a (generally small) substring
of contiguous characters drawn from a larger string. The _n_ in n-gram corresponds
to its length. Thus, a _bigram_ contains 2 characters, while a _trigram_ contains
3.

The following is the list of all bigrams and trigrams for the (cool!) word _nobocoder_:

|Bigrams|Trigrams|
|:-----:|:------:|
|no|nob|
|ob|obo|
|bo|boc|
|oc|oco|
|co|cod|
|od|ode|
|de|der|
|er|

It's not hard to see that _nobocoder_ and _novocoder_ share 7 of their 8 bigrams and
6 of their 7 trigrams. N-Gram distance is, in fact, a similarity metric in its own right (albeit somewhat less effective for our purposes than, say, Levenshtein).

For short strings such as dictionary words, restricting word pairs to those sharing
at least _one_ bigram expunges a surprisingly high number of otherwise wasteful comparisons.

Equipped with this knowledge we can now identify the data structures needed by our
basic algorithm.

## Spelling Suggestion Algorithm ##

Spelling suggestion requires two operations:

- Determining whether a given word occurs in the dictionary or not
- For a given unknown word, determining what known words are sufficiently
  similar to it

### Word Membership ###
A `Set` is the appropriate data structure to efficiently ascertain word
membership. In Scala this would look like:

```scala
val dictionary = Set("academic", "academy", "accent", "accept", "accident", "account", "accountant", "acid", "count")
...
val someWord = "..."
...
println(if (dictionary contains someWord) "Sure enough" else s"Whaddaya mean $someWord?") 
```

### Finding Similar Words ###

Finding similar words is a bit more involved: we need a `Map` connecting each
bigram to the `Set` of words in which it occurs.

Thus, for our above dictionary fragment, the bigram-to-wordset map would be:

|Bigram|Word Set|
|:----:|---------|
|ac|academic, academy, accent, accept, accident, account, accountant, acid|
|ad|academic, academy|
|an|accountant|
|ca|academic, academy|
|cc|accent, accept, accident, account, accountant|
|ce|accent, accept|
|ci|accident, acid|
|co|account, accountant, count|
|de|academic, academy, accident|
|em|academic, academy|
|en|accent, accident|
|ep|accept|
|ic|academic|
|id|accident, acid|
|mi|academic|
|my|academy|
|nt|accent, accident, account, accountant, count|
|ou|account, accountant, count|
|pt|accept|
|ta|accountant|
|un|account, accountant, count|

Given this map, let's consider the typo _accet_. This typo has the following
bigrams:

|Typo|Bigrams|
|----|:-----:|
|accet|ac|
||cc|
||ce|
||et|

Extracting these bigrams from our map we obtain:

|Typo|Bigram|Related Words|
|----|:----:|-----|
|accet|ac|academic, academy, accent, accept, accident, account, accountant, acid|
||cc|accent, accept, accident, account, accountant|
||ce|accent, accept|
||et|_no matching words_|

The union set of these related words is:

|Typo|Related Words|
|----|-------------|
|accet|academic|
||academy|
||accent|
||accept|
||accident|     
||account|
||accountant|                                                                                                                                                                  ||acid|

We can now compare our typo _accet_ with each of these related words using
Levenshtein:

|Typo|Word|Score|
|----|----|-----|
|accet|accent|0.8333|
||accept|0.8333|
||accident|0.6250|
||account|0.5714|
||academy|0.4286|
||accountant|0.4000|
||acid|0.4000|
||academic|0.3750|

With a minimum similarity of `0.75` only the words _accent_ and _accept_ would be
returned as suggestions.

The data structure needed for our purposes is a `Map` where the keys are bigrams (`String`) and the values are the list of words containing the bigram
(`Seq[String]`). In Scala this may look like:

```scala
val ngram2words: Map[String, Seq[String]] = ... // Initialize map of ngram to word list here
...
def ngram(word: String, length: Int = 2): Seq[String] = ... // Extract n-grams from word for a given length
...
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance
val minSimilarity = 0.75
...
val typo = "novocoder"
...
val suggestions: Seq[String] =
  ngram(typo). // extract bigrams from typo
  flatMap(ngram2words). // replace each bigram by its associated words
  distinct. // remove duplicate words
  map(word => (word, levenshtein.getDistance(word, typo))) // compare each word with typo
  filter(_._2 >= minSimilarity). // remove words not sufficiently similar
  sortBy(-_._2). // sort in descending similarity order (more similar words first)
  map(_._1) // extract only the word, leaving out the similarity score
```

Don't worry about the seemingly cryptic syntax; as we advance in our presentation
things will fall neatly into place.

For now, note how compact this algorithm looks thanks to Scala's functional collections!

## Scala as a Scripting Language ##

Despite being a strongly-typed language Scala has the refreshing feel of dynamic languages like Ruby and Python. Type annotations, in particular, are most often optional due to Scala's
[_type inference_](http://en.wikipedia.org/wiki/Type_inference).

Scala can also be used as a scripting language: free-form scripts don't need to define
enclosing classes and can be run without a compilation step.

For this Scala has an interactive mode called the [_REPL_](http://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop)
(read-eval-print loop,) a powerful concept pioneered by Lisp and now quite common among
scripting and functional languages. REPL's foster a development style dubbed
[_exploratory programming_](http://en.wikipedia.org/wiki/Exploratory_programming)
which fits functional programming especially well.

To illustrate Scala's feel as a scripting language let's write a quick'n'dirty
implementation of the naïve approach to spelling suggestion. For this, we'll asume we have
a disk file containing the dictionary, one word per line.

```scala
// Load dictionary from file
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet

// Build the similarity scorer
val minSimilarity = 0.75
val levenshtein = new org.apache.lucene.search.spell.LevensteinDistance

// Set the terms being examined
val terms = Seq("good", "word", "here", "badd", "wurd", "herre")

terms foreach { term =>
  if (!(dictionary contains term))
    dictionary foreach { knownWord =>
      if (levenshtein.getDistance(term, knownWord) >= minSimilarity)
        println(s"$term: did you mean $knownWord?")
    }
}
```

When run, the above script will output:

>
badd: did you mean bald?  
badd: did you mean band?  
badd: did you mean bade?  
badd: did you mean bad?  
badd: did you mean bard?  
badd: did you mean add?  
badd: did you mean baud?  
wurd: did you mean kurd?  
wurd: did you mean curd?  
wurd: did you mean ward?  
wurd: did you mean turd?  
wurd: did you mean word?  
herre: did you mean here?  

Let's dissect this script.

Populating the dictionary from disk to an efficient (hash) set is refreshingly simple!

```scala
val dictionary = io.Source.fromFile("files/words.txt").getLines.toSet
```

Scala provides the `io.Source` class to perform read operations on a variety of input
sources. Instead of using the qualified class name we could import it as in:

```scala
import io.Source
val dictionary = Source.fromFile("files/words.txt").getLines.toSet
```

The `fromFile` function opens a file for reading and returns an instance of the `Source` 
class. This class has a `getLines` method yielding a string iterator to read each
line in the file. `Iterator`, in turn, provides a `toSet` method that builds a `Set`
suitable for quick membership testing. Cool!

Next, we build a similarity scorer using Lucene's implementation of the Levenshtein
(or, as they prefer to write it, _levenstein_) algorithm:

```scala
// Build the similarity scorer
import org.apache.lucene.search.spell._

val minSimilarity = 0.75
val levenshtein = new LevensteinDistance
```

`LevensteinDistance` provides a `getDistance` method that computes the similarity between
2 strings:

```scala
levenshtein.getDistance("nobocder", "novocoder") // 0.8888889
```

We then populate a list of test terms to exercise our suggestion approach:

```scala
val terms = Seq("good", "word", "here", "badd", "wurd", "herre")
```

We're now ready to visit each term and test if it exists in the dictionary;
if it doesn't we traverse the dictionary comparing each word and selecting it
if similar to the term:

```scala
terms foreach { term =>
  if (!(dictionary contains term))
    dictionary foreach { knownWord =>
      if (levenshtein.getDistance(term, knownWord) >= minSimilarity)
        println(s"$term: did you mean $knownWord?")
    }
}
```

Note how instead of saying _`foreach term in terms { ... }`_ in Scala we say
`terms foreach { ... }`. This is so because `foreach` is a _method_ defined
on collections that takes a block of code as argument. Thus, if we wanted to
print all terms we'd say:

```scala
terms.foreach(println)
```

Hmm... this code reveals the process but not so much the _intention_.

We want to filter out the terms occurring in the dictionary and then, further, we want to
filter out the dictionary words that are not sufficiently similar to each unkown term.


