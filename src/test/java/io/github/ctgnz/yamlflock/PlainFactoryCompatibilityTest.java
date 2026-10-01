package io.github.ctgnz.yamlflock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * {@link FlockYamlFactory} is documented as usable anywhere a {@link YAMLFactory} would go, so for any value that carries no styling annotation its output has to match plain
 * Jackson's exactly. This pins that, which is a compatibility contract rather than a statement about styling - which is why it is a unit test and not a Cucumber scenario. The
 * features describe what the annotations do; this describes what the library promises not to change.
 * <p>
 * A root-level scalar is the case that mattered: the generator used to dereference the write context's current value unguarded, and that value is null when nothing owns the
 * scalar, so {@code writeValueAsString("hello")} failed with a {@link NullPointerException} where a plain factory returned {@code hello}.
 */
class PlainFactoryCompatibilityTest {

    static Stream<Arguments> unannotatedValues() {
        return Stream.of(arguments("a root-level string", "hello"), arguments("a root-level string that resolves as a boolean", "no"), arguments("a root-level string with a leading zero", "0001"),
            arguments("a root-level string with a reserved character", "Title: Subtitle"), arguments("a root-level integer", 42), arguments("a root-level boolean", true),
            arguments("a list of strings", List.of("a", "b")), arguments("a map of strings", Map.of("key", "value")), arguments("a list nested in a map", Map.of("key", List.of("a", "b"))),
            arguments("a map nested in a list", List.of(Map.of("key", "value"))), arguments("an empty list", List.of()), arguments("an empty map", Map.of()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unannotatedValues")
    void isWrittenExactlyAsAPlainFactoryWouldWriteIt(String description, Object value) throws Exception {
        String flock = mapper(new FlockYamlFactory(builder())).writeValueAsString(value);
        String plain = mapper(builder().build()).writeValueAsString(value);
        assertThat(flock, is(plain));
    }

    private static com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder builder() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setWidth(480);
        options.setLineBreak(LineBreak.UNIX);
        return YAMLFactory.builder().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).dumperOptions(options);
    }

    private static ObjectMapper mapper(YAMLFactory factory) {
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
        return mapper;
    }

}
