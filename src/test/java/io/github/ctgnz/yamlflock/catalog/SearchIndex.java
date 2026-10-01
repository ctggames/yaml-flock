package io.github.ctgnz.yamlflock.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The catalogue's lookup tables: title, author and year, each to the ISBNs that match.
 * <p>
 * A map whose values are lists of scalars, which is a shape none of the other types reach - the map is block, and each list inline. Present because a catalogue would have one, and
 * because it is the combination most likely to be got wrong by a generator that treats "is this a collection" as one question rather than two.
 */
public class SearchIndex {

    private final Map<String, List<String>> byTitle = new LinkedHashMap<>();
    private final Map<String, List<String>> byAuthor = new LinkedHashMap<>();
    private final Map<Integer, List<String>> byYear = new LinkedHashMap<>();

    public SearchIndex title(String term, String... isbns) {
        byTitle.put(term, List.of(isbns));
        return this;
    }

    public SearchIndex author(String term, String... isbns) {
        byAuthor.put(term, List.of(isbns));
        return this;
    }

    public SearchIndex year(int term, String... isbns) {
        byYear.put(term, List.of(isbns));
        return this;
    }

    public Map<String, List<String>> getByTitle() {
        return byTitle;
    }

    public Map<String, List<String>> getByAuthor() {
        return byAuthor;
    }

    public Map<Integer, List<String>> getByYear() {
        return byYear;
    }

}
