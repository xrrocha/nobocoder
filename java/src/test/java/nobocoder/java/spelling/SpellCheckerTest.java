package nobocoder.java.spelling;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class SpellCheckerTest {
	@Test
	public void removesWordsWithoutSuggestion() {
		Set<String> words = new HashSet<>(Arrays.asList(
				"be", "great", "iz", "misunderstood"));
		
		Map<String, Collection<String>> suggestionMap = new HashMap<>();
		suggestionMap.put("iz", Arrays.asList("is"));
		
		SpellChecker spellChecker = new SpellChecker() {
			public Optional<Collection<String>> suggestionsFor(String word) {
				if (suggestionMap.containsKey(word)) {
					return Optional.of(suggestionMap.get(word));
				} else {
					return Optional.<Collection<String>> empty();
				}
			}
		};

		assertEquals(suggestionMap, spellChecker.suggestionsFor(words));
	}
	
	// TODO Have Mockito invoke real method for interface default methods
	public void mockitoRemovesWordsWithoutSuggestion() {
		Set<String> words = new HashSet<>(Arrays.asList(
				"be", "great", "iz", "misunderstood"));
		
		Map<String, Collection<String>> suggestionMap = new HashMap<>();
		suggestionMap.put("iz", Arrays.asList("is"));
		
		SpellChecker spellChecker = mock(SpellChecker.class);

		Map<Boolean, List<String>> includedExcluded = words.stream().
			collect(Collectors.partitioningBy(w -> suggestionMap.containsKey(w)));

		includedExcluded.get(true).stream().
		forEach(w -> when(spellChecker.suggestionsFor(w)).
				 	 thenReturn(Optional.of(suggestionMap.get(w))));

		includedExcluded.get(false).stream().
			forEach(w -> when(spellChecker.suggestionsFor(w)).
						 thenReturn(Optional.<Collection<String>> empty()));
		
		when(spellChecker.suggestionsFor(words)).thenCallRealMethod();
		
		assertEquals(suggestionMap, spellChecker.suggestionsFor(words));
	}
}
