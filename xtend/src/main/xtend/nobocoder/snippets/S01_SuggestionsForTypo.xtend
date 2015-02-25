package nobocoder.snippets

import org.apache.lucene.search.spell.LevensteinDistance
import org.eclipse.xtend.lib.annotations.Data


class S01_SuggestionsForTypo {
    def static void main(String...args) {
        // Similarity metric
        val minSimilarity = 0.75
        val levenshtein = new LevensteinDistance

        // The typo in question
        val typo = 'accet'

        // Extract bigrams from typo
        val ngramLength = 2
        val typoNgrams =
                // Build a range from 0 to last allowable position
                (0 ..< typo.length - ngramLength + 1)
                    // Extract ngram starting at each successive position
                    .map[pos | typo.substring(pos, pos + ngramLength)]
                    // Make into set to remove dups
                    .toSet

        val suggestions = typoNgrams
                // Select only ngrams appearing in the ngram-to-word map
                .filter[ngram | ngram2Words.containsKey(ngram)]
                // Produce the set of all words sharing ngrams with typo
                .map[ngram | ngram2Words.get(ngram)]
                // Flatten nested collection
                .flatten
                // Make word list into a set to remove dups
                .toSet
                // Convert each word into a word/similarity score pair
                .map[word | new WordScore(word, levenshtein.getDistance(word, typo))]
                // Remove scores below minimum allowed similarity
                .filter[wordScore | wordScore.score >= minSimilarity]
                // Sort descending so as to show more similar words first
                .sortBy[wordScore | -wordScore.score]
                // Extract word only, removing score
                .map[wordScore | wordScore.word]
                

        // Prints: Suggestions for 'accet': [accent, accept]
        println('''Suggestions for '«typo»': «suggestions»''')
    }
    
    // Word/score pair data class
    @Data static class WordScore {
      String word
      double score
    }
    
    static val ngram2Words = #{
        'ac' -> #{'academic', 'academy', 'accent', 'accept',
                  'accident', 'account', 'accountant', 'acid'},
        'ad' -> #{'academic', 'academy'},
        'an' -> #{'accountant'},
        'ca' -> #{'academic', 'academy'},
        'cc' -> #{'accent', 'accept', 'accident',
                  'account', 'accountant'},
        'ce' -> #{'accent', 'accept'},
        'ci' -> #{'accident', 'acid'},
        'co' -> #{'account', 'accountant', 'count'},
        'de' -> #{'academic', 'academy', 'accident'},
        'em' -> #{'academic', 'academy'},
        'en' -> #{'accent', 'accident'},
        'ep' -> #{'accept'},
        'ic' -> #{'academic'},
        'id' -> #{'accident', 'acid'},
        'mi' -> #{'academic'},
        'my' -> #{'academy'},
        'nt' -> #{'accent', 'accident', 'account',
                  'accountant', 'count'},
        'pt' -> #{'accept'},
        'ta' -> #{'accountant'},
        'un' -> #{'account', 'accountant', 'count' }
    }
}