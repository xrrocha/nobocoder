package nobocoder.snippets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.lucene.search.spell.LevensteinDistance;

public class S01_SuggestionsForTypo {
    final static Map<String, Set<String>> ngram2Words = buildNgram2Words();
    
    public static void main(String... args) {
        // Similarity metric
        final double minSimilarity = 0.75;
        final LevensteinDistance levenshtein = new LevensteinDistance();

        // The typo in question
        final String typo = "accet";

        // Extract bigrams from typo
        int ngramLength = 2;
        Set<String> typoNgrams =
                // Build a range from 0 to last allowable position
                IntStream.range(0, typo.length() - ngramLength + 1)
                    // Extract ngram starting at each successive position
                    .mapToObj(pos -> typo.substring(pos, pos + ngramLength))
                    // Collect ngrams into set to remove dups
                    .collect(Collectors.toSet());

        // Word/score pair data class
        class WordScore {
          String word;
          double score;
          WordScore(String word, double score) {
            this.word = word;
            this.score = score;
          }
        }

        List<String> suggestions = typoNgrams.stream()
                // Select only ngrams appearing in the ngram-to-word map
                .filter(ngram2Words::containsKey)
                // Produce the list of all words sharing ngrams with typo
                .flatMap(ngram -> ngram2Words.get(ngram).stream())
                // Make word list into a set to remove dups
                .collect(Collectors.toSet()).stream()
                // Convert each word into a word/similarity score pair
                .map(word -> new WordScore(word, levenshtein.getDistance(word, typo)))
                // Remove scores below minimum allowed similarity
                .filter(wordScore -> wordScore.score >= minSimilarity)
                // Sort descending so as to show more similar words first
                .sorted((ws1, ws2) -> (int) (ws2.score - ws1.score))
                // Extract word only, removing score
                .map(wordScore -> wordScore.word)
                // Collect results into a list
                .collect(Collectors.toList());

        // Prints: Suggestions for 'accet': [accent, accept]
        System.out.println("Suggestions for '" + typo + "': " + suggestions);
    }

    static Map<String, Set<String>> buildNgram2Words() {
        Map<String, Set<String>> ngram2Words = new HashMap<>();
        ngram2Words.put("ac", new HashSet<>(Arrays.asList(
                "academic", "academy", "accent", "accept", "accident", "account", "accountant", "acid")));
        ngram2Words.put("ad", new HashSet<>(Arrays.asList(
                "academic", "academy")));
        ngram2Words.put("an", new HashSet<>(Arrays.asList(
                "accountant")));
        ngram2Words.put("ca", new HashSet<>(Arrays.asList(
                "academic", "academy")));
        ngram2Words.put("cc", new HashSet<>(Arrays.asList(
                "accent", "accept", "accident", "account", "accountant")));
        ngram2Words.put("ce", new HashSet<>(Arrays.asList(
                "accent", "accept")));
        ngram2Words.put("ci", new HashSet<>(Arrays.asList(
                "accident", "acid")));
        ngram2Words.put("co", new HashSet<>(Arrays.asList(
                "account", "accountant", "count")));
        ngram2Words.put("de", new HashSet<>(Arrays.asList(
                "academic", "academy", "accident")));
        ngram2Words.put("em", new HashSet<>(Arrays.asList(
                "academic", "academy")));
        ngram2Words.put("en", new HashSet<>(Arrays.asList(
                "accent", "accident")));
        ngram2Words.put("ep", new HashSet<>(Arrays.asList(
                "accept")));
        ngram2Words.put("ic", new HashSet<>(Arrays.asList(
                "academic")));
        ngram2Words.put("id", new HashSet<>(Arrays.asList(
                "accident", "acid")));
        ngram2Words.put("mi", new HashSet<>(Arrays.asList(
                "academic")));
        ngram2Words.put("my", new HashSet<>(Arrays.asList(
                "academy")));
        ngram2Words.put("nt", new HashSet<>(Arrays.asList(
                "accent", "accident", "account", "accountant", "count")));
        ngram2Words.put("pt", new HashSet<>(Arrays.asList(
                "accept")));
        ngram2Words.put("ta", new HashSet<>(Arrays.asList(
                "accountant")));
        ngram2Words.put("un", new HashSet<>(Arrays.asList(
                "account", "accountant", "count")));
        return ngram2Words;
    }
}
