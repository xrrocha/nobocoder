package nobocoder.java.spelling.ngram;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NGramUtils {
    public static List<String> ngrams(String string, int ngramLength) {
        return Arrays.stream(words(string)).
            flatMap(w -> sliding(w, ngramLength)).
            collect(Collectors.toList());
    }
    
    public static String[] words(String string) {
        return string.trim().toLowerCase().split("\\s++");
    }

    public static Stream<String> sliding(String string, int length) {
        return IntStream.range(0, string.length()).boxed().
                filter(i -> string.length() >= i + length).
                map(i -> string.substring(i, i + length));
    }
}
