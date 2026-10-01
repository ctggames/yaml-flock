package io.github.ctgnz.yamlflock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ctgnz.yamlflock.catalog.Author;
import io.github.ctgnz.yamlflock.catalog.Book;
import io.github.ctgnz.yamlflock.catalog.Edition;
import io.github.ctgnz.yamlflock.catalog.Format;

/**
 * Writing a force-quoted property must not change the generator's own configuration for everything written after it.
 * <p>
 * Forcing quotes means turning {@code MINIMIZE_QUOTES} off for one value, which leaves the question of what to turn it back to. Restoring whatever was found is the only answer
 * that respects a caller who chose to run without it - a legitimate choice, meaning fully quoted output.
 * <p>
 * A unit test rather than a Cucumber scenario because the thing being varied is a generator feature rather than anything in the catalogue domain, and teaching the feature files a
 * configuration vocabulary for one scenario would cost more than it explains. It is also the test whose absence let this through: every scenario runs with {@code MINIMIZE_QUOTES}
 * enabled, so not one of them is able to see it.
 */
class ForceQuoteStateTest {

    /** A book whose forced {@code isbn} and {@code title} are written before its unforced author and edition properties. */
    private static Book book() {
        return new Book("0306406152", "Godel Escher Bach", 1979).by(new Author("Douglas Hofstadter", "0001", 1945)).printed("first", new Edition(Format.HARDBACK, 777, 1979));
    }

    private static String written(boolean minimizeQuotes) throws Exception {
        ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().minimizeQuotes(minimizeQuotes).build());
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper.writeValueAsString(book());
    }

    @Test
    void aForcedPropertyDoesNotTurnQuoteMinimisingOnForTheRestOfTheDocument() throws Exception {
        String yaml = written(false);

        assertThat(yaml, containsString("isbn: \"0306406152\""));
        assertThat(yaml, containsString("name: \"Douglas Hofstadter\""));
        assertThat(yaml, containsString("sortKey: \"0001\""));
        assertThat(yaml, containsString("format: \"HARDBACK\""));

        assertThat(yaml, not(containsString("name: Douglas Hofstadter")));
        assertThat(yaml, not(containsString("sortKey: '0001'")));
        assertThat(yaml, not(containsString("format: HARDBACK")));
    }

    @Test
    void withQuoteMinimisingOnTheForcedPropertyIsStillTheOnlyQuotedOne() throws Exception {
        String yaml = written(true);

        assertThat(yaml, containsString("isbn: \"0306406152\""));
        assertThat(yaml, containsString("title: \"Godel Escher Bach\""));
        assertThat(yaml, containsString("name: Douglas Hofstadter"));
        assertThat(yaml, containsString("format: HARDBACK"));
    }

    /**
     * The two configurations must not produce the same document.
     * <p>
     * Stated on its own because it is the shape the original defect took: with the feature switched back on unconditionally, output was byte-identical whether the caller had
     * enabled it or not, which is the clearest possible evidence that their choice was being discarded.
     */
    @Test
    void theTwoConfigurationsDoNotProduceTheSameDocument() throws Exception {
        assertThat(written(false), not(containsString(written(true))));
    }

}
