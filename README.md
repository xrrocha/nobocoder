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
val dictionary: Set[String] = ... // Initialize dictionary here 
def isKnown(word: String): Boolean = dictionary.contains(word)
...
if (isKnown(term)) println("Sure enough") else println(s"Whaddaya mean $term?")
```

Disgression: if only to illustrate a somewhat more idiomatic use of Scala, the above
is equivalent to:
```scala
val dictionary = ... // Initialize dictionary to a Set-returning expression
def isKnown(word: String) = dictionary contains word
...
println(if (isKnown(term)) "Sure enough" else s"Whaddaya mean $term?")
```

### Finding Similar Words ###

Finding similar words is a bit more involved: we need a `Map` connecting each
bigram to the `Set` of words in which it occurs.

Thus, for our above dictionary fragment, the bigram-to-wordset map would be:

|Bigram|Word Set|
|------|---------|
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
|----|-------|
|accet|ac|
||cc|
||ce|
||et|

Extracting these bigrams from out map we obtain:

|Typo|Bigram|Related Words|
|----|------|-----|
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
||accountant|                                                                                                                                                                  
||acid| 

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




