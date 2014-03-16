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

>
Note: instead of saying _`foreach term in terms { ... }`_ in Scala we say
`terms foreach { ... }`. This is so because `foreach` is a _method_ defined
on collections. Ths method takes a block of code as argument. Thus, if we want
to print all terms we say `terms.foreach(println)`. Expressive!

In the above code snippet we filter out the terms occurring in the dictionary and
then, for each unknown word, we filter out dictionary words not sufficiently similar.
We achieve this by means of `foreach` and `if`.

In functional programming, common operations on collections are implemented as functions
(in Scala, methods) rather than requiring the programmer to endlessly write loops and
conditionals.

Thus, a more idiomatic way to write our word-collecting loop is:

```scala
terms.
  filter(term => !(dictionary contains term)).
  foreach { term =>
    val similars = dictionary filter { word =>
      levenshtein.getDistance(term, word) >= minSimilarity
    }
    if (similars isEmpty)
      println(s"Whaddaya mean '$term'?")
    else
      println(s"$term: you probably meant one of $similars")
  }
```

This will output:
>
badd: you probably meant one of Set(bald, band, bade, bad, bard, add, baud)  
wurd: you probably meant one of Set(kurd, curd, ward, turd, word)  
herre: you probably meant one of Set(here)  
Whaddaya mean 'notaword'?

This humble code snippet has a wealth of useful information for us. Let's embark!

Our first step is to filter out terms appearing in the dictionary:

```scala
terms.filter(term => !(dictionary contains term))
```

Here, we visit each element in the `terms` collection selecting only those elements matching
the `filter` predicate (namely, that the given term is not contained in the dictionary.)

`filter` is a collection method that takes as argument a block of code to be executed for
each element in the collection. This block of code must return a `Boolean` value indicating
whether the given element satisfies a predicate or not. If it does, the element is included
in the output collection; otherwise, it is omitted.

Blocks of code passed as arguments are referred to as [_lambdas_](http://en.wikipedia.org/wiki/Anonymous_function). This construction is very familiar to
Rubyists and Pythonistas and has found its way into strongly typed languages such as C#,
C++ and Java.

In order to refer to the current element inside our lambda we start the code block with
a variable name followed by a fat arrow, followed by the actual predicate:

```scala
term => !(dictionary contains term)
```

Any variable name will do for the lambda argument as long as we use it consistently in the
body:

```scala
someWord => !(dictionary contains someWord)
```

In scala, when the lambda argument is used only once in the body it can be replaced by
the underscore anonymous variable (`_`). Thus our filter expression could be rewritten as:

```scala
terms.filter(!dictionary.contains(_))
```

Scala's underscore is roughly equivalent to Groovy's and Xtend's `it` implicit lambda
argument.

In our specific case, because we're negating the dictionary membership test, we could use
the `filterNot` function instead of `filter`:

```scala
terms.filterNot(dictionary.contains(_))
```

This, in turn, opens the way for one further simplification: when a lambda body consists
of a single function whose only argument is the lambda argument itself then we can pass
just the function name. Thus, the above is equivalent to:

```scala
terms.filterNot(dictionary.contains)
```

This may look a bit terse at first but, for the trained eye, it's actually much more legible
and informative.

Why? Functional programming emphasizes _what_ is to be done rather than _how_ to do it. This
is achieved by expressing computations as successive data transformations rather than
explicit operations _upon_ data. And data transformations are embodied as -you guessed it-
functions.

Thus, when we see `terms.filterNot(dictionary.contains)` it reads like "weed out terms
not contained in the dictionary." We emphasize what the function does rather than how
to call it.

This concept is not totally alien to imperative programming. In venerable C, for instance,
there are pointers to functions allowing us to pass functions around as arguments. That's
why we can write a generic binary search algorithm where the only "moving part" is the actual
element comparison (passed as an argument to the algorithm via a pointer to function.)

```c
void * binary_search (
    void *key, // The search key
    void *base, // The sorted array's initial address
    int num, // The sorted array's number of elements
    int width, // The width of each element in the array
    int (*compare)(void *, void *) // The comparison function
)
{
    void *lo = base;
    void *hi = base + (num - 1) * width;
    void *mid;
    int half;
    int result;

    while (lo <= hi)
        if (half = num / 2) {
            mid = lo + (num & 1 ? half : (half - 1)) * width;
            if (!(result = (*compare)(key,mid)))
                return(mid);
            else if (result < 0) {
                hi = mid - width;
                num = num & 1 ? half : half-1;
            }
            else    {
                lo = mid + width;
                num = half;
            }
        }
        else if (num)
            return((*compare)(key,lo) ? NULL : lo);
        else
            break;

    return(NULL);
}
```

Likewise, processing data through successive transformations on collections is a time-honored
concept. Let's recall the classic, sales-pitch Unix example:

```bash
cat *.txt |  # Collect the text files
tr A-Z a-z |  # Make all words lowercase
tr -cs a-z '\012' |  # Put each word on a separate line
sort -u -o dictionary.txt  # Order by word -suppressing duplicates- onto dictionary file
```

Wow! We can build a dictionary with four simple commands in a single pipeline.

This style of collection manipulation rings a bell... yes: our early formulation of the
spelling suggestion algorithm!

```scala
val suggestions: Seq[String] =
  ngram(typo). // extract bigrams from typo
  flatMap(ngram2words). // replace each bigram by its associated words
  distinct. // remove duplicate words
  map(word => (word, levenshtein.getDistance(word, typo))) // compare each word with typo
  filter(_._2 >= minSimilarity). // remove words not sufficiently similar
  sortBy(-_._2). // sort in descending similarity order (more similar words first)
  map(_._1) // extract only the word, leaving out the similarity score
```















