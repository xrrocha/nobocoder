package nobocoder.java.spelling.ngram;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class NGramUtilsTest {
    @Test
    public void slidesString() {
        String string = "reverberation";
        
        List<String> expectedNgram2 = Arrays.asList(
                "re", "ev", "ve", "er", "rb", "be",
                "er", "ra", "at", "ti", "io", "on");
        List<String> actualNgram2 = NGramUtils.sliding(string, 2).collect(Collectors.toList());
        assertEquals(expectedNgram2, actualNgram2);
            
        List<String> expectedNgram3 = Arrays.asList(
                "rev", "eve", "ver", "erb", "rbe",
                "ber", "era", "rat", "ati", "tio", "ion");
        List<String> actualNgram3 = NGramUtils.sliding(string, 3).collect(Collectors.toList());
        assertEquals(expectedNgram3, actualNgram3);
    }
}
