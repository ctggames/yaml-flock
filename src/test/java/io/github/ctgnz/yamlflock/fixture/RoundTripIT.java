package io.github.ctgnz.yamlflock.fixture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.github.ctgnz.yamlflock.FlockYamlFactory;
import io.github.ctgnz.yamlflock.catalog.Catalog;

/**
 * Reads the committed sample document and writes it back, over a hundred-book catalogue read off disk rather than built in memory.
 * <p>
 * Two claims, and the second is one the README makes that nothing else tests:
 * <ol>
 * <li>a document this library wrote is read and written back <strong>byte for byte</strong> - the property that matters for a file under source control, because regeneration must
 * not produce a diff nobody asked for</li>
 * <li>the same document is readable by a <strong>plain {@link YAMLFactory}</strong>, into a graph equivalent to the one this library's own parser produces</li>
 * </ol>
 * <p>
 * The second is checked by reading with a plain factory and then writing with this library's, comparing against the fixture. That is a stronger statement than "it parses without
 * throwing", and it needs no {@code equals} on the model: if the plain parser had lost or altered anything, the re-serialised bytes would differ.
 * <p>
 * <strong>The fixture is a baseline, not an output.</strong> It is committed, and the test never rewrites it - a document this library generates and then compares against itself
 * would only prove self-consistency. Its value is failing when the shape of the output changes, so regenerating it is a deliberate, reviewed act:
 *
 * <pre>
 * mvn verify -Dyamlflock.fixture.record=true
 * </pre>
 * <p>
 * which rewrites the file and skips the comparison for that run. Review the diff before committing it.
 * <p>
 * The fixture is written with the library's default LF endings, and {@code .gitattributes} pins it to LF on checkout. Without that pin, {@code core.autocrlf} would hand Windows a
 * CRLF copy and Linux an LF one, and a byte-exact comparison would pass on one and fail on the other.
 */
class RoundTripIT {

    private static final Path FIXTURE = Path.of("src", "test", "resources", "fixtures", "catalog.yml");

    private static final String RECORD = "yamlflock.fixture.record";

    /** This library's mapper, on the builder's defaults - which is what a consumer gets, so it is what the fixture should be written by. */
    private static ObjectMapper flock() {
        ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().build());
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper;
    }

    /** A plain Jackson YAML mapper, carrying no knowledge of this library at all. */
    private static ObjectMapper plain() {
        ObjectMapper mapper = new ObjectMapper(YAMLFactory.builder().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build());
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper;
    }

    private static boolean recording() {
        return Boolean.parseBoolean(System.getProperty(RECORD, "false"));
    }

    private static byte[] fixture() throws IOException {
        if (recording()) {
            Files.createDirectories(FIXTURE.getParent());
            byte[] written = flock().writeValueAsString(CatalogFixture.catalogue()).getBytes(StandardCharsets.UTF_8);
            Files.write(FIXTURE, written);
            System.out.println("Recorded " + FIXTURE.toAbsolutePath() + " (" + written.length + " bytes) - review the diff before committing it");
            return written;
        }
        return Files.readAllBytes(FIXTURE);
    }

    @Test
    void theFixtureIsThereAndIsASubstantialDocument() throws Exception {
        byte[] bytes = fixture();
        assertThat(bytes.length, is(greaterThan(10_000)));
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertThat(text.lines().count(), is(greaterThan((long) CatalogFixture.BOOKS)));
    }

    @Test
    void readingAndWritingItBackIsByteIdentical() throws Exception {
        byte[] onDisk = fixture();
        Catalog read = flock().readValue(onDisk, Catalog.class);
        byte[] written = flock().writeValueAsString(read).getBytes(StandardCharsets.UTF_8);
        assertIdentical(onDisk, written);
    }

    @Test
    void aPlainYamlFactoryReadsItIntoAnEquivalentGraph() throws Exception {
        byte[] onDisk = fixture();
        Catalog read = plain().readValue(onDisk, Catalog.class);
        byte[] written = flock().writeValueAsString(read).getBytes(StandardCharsets.UTF_8);
        assertIdentical(onDisk, written);
    }

    /**
     * Asserts the two documents are byte for byte the same, reporting the first line that differs.
     * <p>
     * Comparing the whole documents with a plain equality matcher would be correct and useless: the failure message would be two copies of a hundred kilobytes of YAML, and the one
     * changed character somewhere inside them is the only thing worth seeing.
     */
    private static void assertIdentical(byte[] expected, byte[] actual) {
        if (Arrays.equals(expected, actual)) {
            return;
        }
        String[] expectedLines = new String(expected, StandardCharsets.UTF_8).lines().toArray(String[]::new);
        String[] actualLines = new String(actual, StandardCharsets.UTF_8).lines().toArray(String[]::new);
        for (int i = 0; i < Math.min(expectedLines.length, actualLines.length); i++) {
            if (!expectedLines[i].equals(actualLines[i])) {
                assertThat("line " + (i + 1) + " of " + expectedLines.length, actualLines[i], is(expectedLines[i]));
            }
        }
        assertThat("line count", actualLines.length, is(expectedLines.length));
        assertThat("byte length", actual.length, is(expected.length));
    }

}
