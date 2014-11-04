package nobocoder.java.spelling;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import nobocoder.java.util.Pair;

public interface SpellChecker {
	Optional<Collection<String>> suggestionsFor(String word);
	
	default Map<String, Collection<String>> suggestionsFor(Collection<String> words) {
		return words.stream().
			map(word -> new Pair<>(word, suggestionsFor(word))).
			filter(p -> p.getValue().isPresent()).
			collect(Collectors.toMap(Pair::getKey, p -> p.getValue().get()));
	}
}
