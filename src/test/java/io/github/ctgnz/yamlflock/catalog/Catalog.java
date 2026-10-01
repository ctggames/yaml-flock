package io.github.ctgnz.yamlflock.catalog;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The root of the harness model: a library catalogue, carrying no style annotation of its own so that it is written in plain block style.
 * <p>
 * Everything nested under it is annotated for a reason the domain supplies, so that a reader of the published scenarios sees why each choice was made rather than a demonstration
 * arranged to hit every branch.
 */
@JsonPropertyOrder({
    "name", "established", "books", "archive", "index"
})
public class Catalog {

    private String name;
    private int established;
    private final List<Book> books = new ArrayList<>();
    private final List<ArchiveEntry> archive = new ArrayList<>();
    private SearchIndex index;

    public Catalog() {
    }

    public Catalog(String name, int established) {
        this.name = name;
        this.established = established;
    }

    public Catalog holding(Book... values) {
        books.addAll(List.of(values));
        return this;
    }

    public Catalog stacked(ArchiveEntry... values) {
        archive.addAll(List.of(values));
        return this;
    }

    public Catalog indexed(SearchIndex searchIndex) {
        this.index = searchIndex;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getEstablished() {
        return established;
    }

    /** A list of block-style objects, so each book is a nested mapping rather than a line. */
    public List<Book> getBooks() {
        return books;
    }

    public List<ArchiveEntry> getArchive() {
        return archive;
    }

    /**
     * Null until a catalogue has been indexed, which is deliberate.
     * <p>
     * {@code NON_NULL} here rather than on the mapper, so the scenarios can pin what an absent value does without that decision being a global setting nobody can see from the
     * model. Jackson's treatment of null and empty collections changed between 2.15 and 2.22 - a null list that used to be written {@code []} is now omitted - and that is exactly
     * the kind of change a fixture catches and a passing build does not.
     */
    @JsonInclude(Include.NON_NULL)
    public SearchIndex getIndex() {
        return index;
    }

}
