package io.github.ctgnz.yamlflock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.LoaderOptions;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.github.ctgnz.yamlflock.catalog.Author;
import io.github.ctgnz.yamlflock.catalog.Book;
import io.github.ctgnz.yamlflock.catalog.Catalog;
import io.github.ctgnz.yamlflock.catalog.Edition;
import io.github.ctgnz.yamlflock.catalog.Format;
import io.github.ctgnz.yamlflock.catalog.Genre;

/**
 * The builder exists to replace a block of configuration that every consuming project was writing by hand, so the test that matters is that it produces exactly what that block
 * produced. {@link #theOneLinerMatchesTheConfigurationItReplaces()} is that test; the rest pin the defaults and the documented behaviour of the escape hatches.
 */
class FlockYamlFactoryBuilderTest {

    /**
     * The configuration both consuming projects wrote by hand, copied verbatim.
     * <p>
     * Kept here rather than reduced to the parts that matter, because a trimmed copy would stop being evidence of what it replaces.
     */
    private static ObjectMapper handConfigured() {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(false);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setCanonical(false);
        options.setWidth(480);
        options.setLineBreak(LineBreak.WIN);
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setCodePointLimit(16 * 1024 * 1024);
        YAMLFactory factory = new FlockYamlFactory(YAMLFactory.builder()
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .loaderOptions(loaderOptions)
            .dumperOptions(options));
        return inclusion(new ObjectMapper(factory));
    }

    private static ObjectMapper inclusion(ObjectMapper mapper) {
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper;
    }

    /** A catalogue exercising every style the annotations can produce, so the comparison is not over a trivial document. */
    private static Catalog catalogue() {
        return new Catalog("Ctgnz Reference Library", 1987).holding(
            new Book("0306406152", "Godel Escher Bach", 1979).with(Genre.PHILOSOPHY, Genre.MATHEMATICS)
                .by(new Author("Douglas Hofstadter", "0001", 1945))
                .printed("first", new Edition(Format.HARDBACK, 777, 1979))
                .printed("anniversary", new Edition(Format.PAPERBACK, 824, 1999)),
            new Book("0140449132", "The Histories: A New Translation", 1954).with(Genre.HISTORY)
                .by(new Author("Herodotus", "0002", -484), new Author("Aubrey de Selincourt", "0003", 1894))
                .printed("revised", new Edition(Format.PAPERBACK, 716, 1972)));
    }

    /**
     * The builder reproduces the configuration it replaces, given the one thing the library deliberately does not assume.
     * <p>
     * That one thing is the line break. The library defaults to {@code UNIX}, because wanting CRLF is a property of a project whose files are already committed with CRLF rather
     * than of YAML, so a consumer asks for it. The claim here is therefore "one line plus an explicit line break" rather than "one line", and it is written that way on purpose:
     * comparing two {@code UNIX} configurations would pass while proving nothing about the configuration anybody actually had.
     */
    @Test
    void theBuilderMatchesTheConfigurationItReplaces() throws Exception {
        String viaBuilder = inclusion(new ObjectMapper(FlockYamlFactory.builder().lineBreak(LineBreak.WIN).build())).writeValueAsString(catalogue());
        String viaHand = handConfigured().writeValueAsString(catalogue());
        assertThat(viaBuilder, is(viaHand));
    }

    @Test
    void buildReturnsAFlockFactoryRatherThanAPlainOne() {
        assertThat(FlockYamlFactory.builder().build(), is(instanceOf(FlockYamlFactory.class)));
    }

    @Test
    void carriesTheDocumentedDefaults() {
        FlockYamlFactoryBuilder builder = FlockYamlFactory.builder();
        DumperOptions dumper = builder.dumperOptions();
        assertThat(dumper.getWidth(), is(FlockYamlFactoryBuilder.DEFAULT_LINE_WIDTH));
        assertThat(dumper.getLineBreak(), is(LineBreak.UNIX));
        assertThat(dumper.getDefaultFlowStyle(), is(FlowStyle.BLOCK));
        assertThat(dumper.isPrettyFlow(), is(false));
        assertThat(dumper.isCanonical(), is(false));
        assertThat(builder.loaderOptions().getCodePointLimit(), is(FlockYamlFactoryBuilder.DEFAULT_CODE_POINT_LIMIT));
    }

    @Test
    void minimizeQuotesIsOnAndTheDocumentMarkerIsOffByDefault() throws Exception {
        String written = inclusion(new ObjectMapper(FlockYamlFactory.builder().build())).writeValueAsString(catalogue());
        assertThat(written, not(containsString("---")));
        assertThat(written, containsString("name: Ctgnz Reference Library"));
    }

    @Test
    void aNamedSettingRefinesOneThingAndLeavesTheRest() {
        FlockYamlFactoryBuilder builder = FlockYamlFactory.builder().lineBreak(LineBreak.WIN).lineWidth(120);
        assertThat(builder.dumperOptions().getLineBreak(), is(LineBreak.WIN));
        assertThat(builder.dumperOptions().getWidth(), is(120));
        assertThat(builder.dumperOptions().getDefaultFlowStyle(), is(FlowStyle.BLOCK));
        assertThat(builder.loaderOptions().getCodePointLimit(), is(FlockYamlFactoryBuilder.DEFAULT_CODE_POINT_LIMIT));
    }

    /**
     * The default is LF, and CRLF is available by asking.
     * <p>
     * Both halves matter. A library defaulting to a platform's line ending would be the wrong place for that requirement to live, and a library that could not produce CRLF at all
     * would push every consumer with committed CRLF files back to assembling {@code DumperOptions} by hand.
     */
    @Test
    void theDefaultLineBreakIsLfAndCrlfIsAvailableByAskingForIt() throws Exception {
        String byDefault = inclusion(new ObjectMapper(FlockYamlFactory.builder().build())).writeValueAsString(catalogue());
        assertThat(byDefault, not(containsString("\r\n")));

        String asked = inclusion(new ObjectMapper(FlockYamlFactory.builder().lineBreak(LineBreak.WIN).build())).writeValueAsString(catalogue());
        assertThat(asked, containsString("\r\n"));
    }

    @Test
    void aNamedSettingAppliedAfterAnInheritedOneStillReachesTheBuilder() {
        FlockYamlFactoryBuilder builder = FlockYamlFactory.builder().enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE).lineWidth(200);
        assertThat(builder.dumperOptions().getWidth(), is(200));
        assertThat(builder.build(), is(instanceOf(FlockYamlFactory.class)));
    }

    /**
     * Pins the documented escape hatch: a whole replacement options object wins over anything named before it.
     * <p>
     * Deliberately not defended against. A caller who reaches for {@code dumperOptions} owns the result, and the point of pinning it is that the behaviour is stated rather than
     * discovered.
     */
    @Test
    void replacingTheOptionsObjectDiscardsNamedSettingsAppliedBeforeIt() {
        DumperOptions replacement = new DumperOptions();
        replacement.setWidth(60);
        FlockYamlFactoryBuilder builder = FlockYamlFactory.builder().lineWidth(400).dumperOptions(replacement);
        assertThat(builder.dumperOptions().getWidth(), is(60));

        FlockYamlFactoryBuilder other = FlockYamlFactory.builder().dumperOptions(replacement).lineWidth(400);
        assertThat(other.dumperOptions().getWidth(), is(400));
    }

}
