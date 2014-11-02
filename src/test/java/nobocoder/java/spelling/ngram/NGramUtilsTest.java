package nobocoder.java.spelling.ngram;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
    
    @Test
    public void separatesWords() {
        String words = " \t Some  words\t@nd   n0nS3ns3   in-between ";
        String[] actualWords = NGramUtils.words(words);
        String[] expectedWords = new String[] { "some", "words", "@nd", "n0ns3ns3", "in-between" };
        assertArrayEquals(expectedWords, actualWords);
    }
    
    @Test
    public void buildsNgram() {
        String string = "\t strong  \t\t  reverberation \t";
        Collection<String> expectedNgrams = new HashSet<>(Arrays.asList(
                "st", "tr", "ro", "on", "ng",
                "re", "ev", "ve", "er", "rb",
                "be", "ra", "at", "ti", "io", "on"));
        Collection<String> actualNgrams = NGramUtils.ngrams(string, 2);
        assertEquals(actualNgrams, expectedNgrams);
    }
}
