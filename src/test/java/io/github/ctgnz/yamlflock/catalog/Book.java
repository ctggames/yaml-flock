package io.github.ctgnz.yamlflock.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import io.github.ctgnz.yamlflock.YamlBlockStyle;
import io.github.ctgnz.yamlflock.YamlFlowStyle;
import io.github.ctgnz.yamlflock.YamlForceQuote;

/**
 * A catalogued book, and the worked example of the only way YAML lets scalars sit on one line beside a block-style map.
 * <p>
 * The class itself carries no style annotation, so it is written in block style. Its scalars live in a {@link Details} record that is flow style, which puts them on a single line;
 * its {@code editions} map is then free to be block, because a <em>block</em> mapping may contain flow content. The reverse is not true - YAML forbids block content inside a flow
 * collection - so annotating this class {@link YamlFlowStyle} and trying to except {@code editions} with {@link YamlBlockStyle} does not work: the override is simply ignored and
 * the whole object comes out inline, the emitter having no way to express it. The nested record is not a workaround for a missing feature; it is the shape the format permits.
 */
@JsonIgnoreProperties({
    "isbn", "title", "year"
})
@JsonPropertyOrder({
    "details", "genres", "authors", "editions"
})
public class Book {

    /**
     * The scalars, on one line.
     * <p>
     * Force-quoted for uniformity rather than for safety. SnakeYAML's quoting checker already protects a value that needs it - drop the annotation and the ISBN is still written
     * {@code '0306406152'}, single-quoted, with its leading zero intact. What the annotation protects against is inconsistency: quoted only where the particular value happens to
     * need it, a catalogue carries {@code isbn: '0306406152'} beside {@code isbn: 030640615X} and the column stops reading as one type.
     * <p>
     * {@code title} is the clearer case of the two, because the values that will need quoting cannot be enumerated in advance. A title may carry a colon, a leading {@code @}, a
     * {@code #}, a comma that falls inside a flow mapping, or an apostrophe - and the apostrophe is the interesting one, because it rules out the single quotes the writer would
     * otherwise reach for, so an unforced {@code L'Etre et le Neant} comes out bare while {@code Structure and Interpretation: 2nd Edition} comes out double-quoted. One column,
     * two renderings, neither wrong. Declaring it forced settles the question once, for every value, however the collection grows.
     * <p>
     * Not for its alphabet, though: the emitter allows Unicode, so accented and non-Latin characters are written literally whether the property is forced or not. That is the
     * plausible-sounding reason to force a text property and it is not a real one - see the scenarios, which pin it.
     */
    @YamlFlowStyle
    @YamlForceQuote(properties = {
        "isbn", "title"
    })
    public record Details(String isbn, String title, int year) {
    }

    private String isbn;
    private String title;
    private int year;
    private final List<Genre> genres = new ArrayList<>();
    private final List<Author> authors = new ArrayList<>();
    private final Map<String, Edition> editions = new LinkedHashMap<>();

    public Book() {
    }

    public Book(String isbn, String title, int year) {
        this.isbn = isbn;
        this.title = title;
        this.year = year;
    }

    public Book with(Genre... values) {
        genres.addAll(List.of(values));
        return this;
    }

    public Book by(Author... values) {
        authors.addAll(List.of(values));
        return this;
    }

    public Book printed(String name, Edition edition) {
        editions.put(name, edition);
        return this;
    }

    /**
     * A handful of values that belong on one line, said here rather than anywhere else.
     * <p>
     * There is no type to annotate: nobody can put an annotation on {@code List<Genre>}, and annotating {@link Genre} itself would say something about the enum rather than about
     * this property. So the statement belongs on the member, which is the case that {@link YamlFlowStyle} being allowed on a field or accessor exists to serve.
     * <p>
     * Without it the only way to get a list inlined is to move it into a flow-style record beside the scalars, which is a change to the shape of the model made for the sake of the
     * shape of the file - and the file should follow the model, not the other way round.
     */
    @YamlFlowStyle
    public List<Genre> getGenres() {
        return genres;
    }

    /** A list of objects, each of them flow style, so the list is block and its entries are one line apiece. */
    public List<Author> getAuthors() {
        return authors;
    }

    /** A block map whose values are flow style - the shape this library exists to make possible. */
    public Map<String, Edition> getEditions() {
        return editions;
    }

    @JsonGetter("details")
    Details getDetails() {
        return new Details(isbn, title, year);
    }

    @JsonSetter("details")
    void setDetails(Details details) {
        this.isbn = details.isbn();
        this.title = details.title();
        this.year = details.year();
    }

}
