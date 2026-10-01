package io.github.ctgnz.yamlflock.cucumber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.ctgnz.yamlflock.FlockYamlFactory;
import io.github.ctgnz.yamlflock.YamlForceQuote;
import io.github.ctgnz.yamlflock.catalog.ArchiveEntry;
import io.github.ctgnz.yamlflock.catalog.Author;
import io.github.ctgnz.yamlflock.catalog.Book;
import io.github.ctgnz.yamlflock.catalog.Catalog;
import io.github.ctgnz.yamlflock.catalog.Edition;
import io.github.ctgnz.yamlflock.catalog.Format;
import io.github.ctgnz.yamlflock.catalog.Genre;
import io.github.ctgnz.yamlflock.catalog.Holdings;
import io.github.ctgnz.yamlflock.catalog.SearchIndex;

/**
 * The vocabulary the feature files are written in, and the one place a mapper is configured.
 * <p>
 * Deliberately thin: every step either builds a piece of the catalogue or asserts the document it produces. Nothing here decides how anything is written - that is the library's
 * job, and a formatting decision hidden in the harness would make the scenarios describe the harness rather than the library.
 */
public class CatalogSteps {

    private final ObjectMapper mapper = newMapper();

    private Book book;
    private ArchiveEntry archiveEntry;
    private Holdings holdings;
    private Catalog catalog;
    private SearchIndex index;
    private String written;

    @Given("a book {string} titled {string} published in {int}")
    public void aBook(String isbn, String title, int year) {
        book = new Book(isbn, title, year);
    }

    @And("it has genres {string}")
    public void itHasGenres(String genres) {
        book.with(Arrays.stream(genres.split(",")).map(String::trim).map(Genre::valueOf).toArray(Genre[]::new));
    }

    @And("it is by {string} sorting as {string} born {int}")
    public void itIsBy(String name, String sortKey, int born) {
        book.by(new Author(name, sortKey, born));
    }

    @And("it has a {string} edition, {word}, {int} pages, published {int}")
    public void itHasAnEdition(String name, String format, int pages, int published) {
        book.printed(name, new Edition(Format.valueOf(format), pages, published));
    }

    @Given("an archive entry {string} holding a {word} edition of {int} pages published {int}")
    public void anArchiveEntry(String shelfMark, String format, int pages, int published) {
        archiveEntry = new ArchiveEntry(shelfMark, new Edition(Format.valueOf(format), pages, published));
    }

    @Given("a holdings record {string} of a {word} edition, {string}, {int} pages, published {int}")
    public void aHoldingsRecord(String shelfMark, String format, String name, int pages, int published) {
        holdings = new Holdings(shelfMark).printed(name, new Edition(Format.valueOf(format), pages, published));
    }

    @When("the holdings record is written as YAML")
    public void theHoldingsRecordIsWritten() throws Exception {
        written = write(holdings);
    }

    @Given("a catalogue {string} established {int}")
    public void aCatalogue(String name, int established) {
        catalog = new Catalog(name, established);
    }

    @And("the catalogue holds that book")
    public void theCatalogueHoldsThatBook() {
        catalog.holding(book);
    }

    @And("the catalogue was founded on a {word} edition of {int} pages published {int}")
    public void theCatalogueWasFoundedOn(String format, int pages, int published) {
        catalog.foundedOn(new Edition(Format.valueOf(format), pages, published));
    }

    @And("the catalogue stacks that archive entry")
    public void theCatalogueStacksThatArchiveEntry() {
        catalog.stacked(archiveEntry);
    }

    @Given("an index of {string} by title to {string}")
    public void anIndexByTitle(String term, String isbns) {
        index = new SearchIndex().title(term, split(isbns));
    }

    @And("indexed by author {string} to {string}")
    public void indexedByAuthor(String term, String isbns) {
        index.author(term, split(isbns));
    }

    @And("indexed by year {int} to {string}")
    public void indexedByYear(int term, String isbns) {
        index.year(term, split(isbns));
    }

    @And("the catalogue is indexed")
    public void theCatalogueIsIndexed() {
        catalog.indexed(index);
    }

    @When("the book is written as YAML")
    public void theBookIsWritten() throws Exception {
        written = write(book);
    }

    @When("the archive entry is written as YAML")
    public void theArchiveEntryIsWritten() throws Exception {
        written = write(archiveEntry);
    }

    @When("the catalogue is written as YAML")
    public void theCatalogueIsWritten() throws Exception {
        written = write(catalog);
    }

    @When("the index is written as YAML")
    public void theIndexIsWritten() throws Exception {
        written = write(index);
    }

    @Then("the YAML is:")
    public void theYamlIs(String expected) {
        assertThat(written, is(expected.strip()));
    }

    private static String[] split(String values) {
        return Arrays.stream(values.split(",")).map(String::trim).toArray(String[]::new);
    }

    private String write(Object value) throws Exception {
        return mapper.writeValueAsString(value).strip();
    }

    /**
     * A mapper configured the way a consumer would configure one.
     * <p>
     * {@code MINIMIZE_QUOTES} is enabled deliberately: it is the setting {@link YamlForceQuote} exists to make exceptions to, and with quoting always on there would be nothing for
     * that annotation to do - the ISBN scenarios would pass against a library that did not work.
     * <p>
     * {@code LineBreak.UNIX} rather than the platform default, so the expected blocks in the feature files mean the same thing wherever the suite runs.
     */
    private static ObjectMapper newMapper() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setCanonical(false);
        options.setWidth(480);
        options.setLineBreak(LineBreak.UNIX);

        YAMLFactory factory = new FlockYamlFactory(YAMLFactory.builder().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).dumperOptions(options));

        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper;
    }

}
