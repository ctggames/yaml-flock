package io.github.ctgnz.yamlflock;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * The entry point: a {@link YAMLFactory} whose generator honours {@link YamlFlowStyle}, {@link YamlBlockStyle} and {@link YamlForceQuote}. Use it anywhere a {@code YAMLFactory}
 * would go.
 *
 * <pre>
 * ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().build());
 * </pre>
 * <p>
 * {@link #builder()} arrives already carrying the settings this library needs, so there is nothing a caller is obliged to remember; {@link FlockYamlFactoryBuilder} lists them and
 * names the few worth changing. A {@link YAMLFactoryBuilder} configured by hand is still accepted through the constructor, for a caller who wants to own every setting.
 * <p>
 * Only writing needs this factory. YAML style is a presentation choice, so a plain {@code YAMLFactory} reads anything written here.
 *
 * @author ctg
 */
public class FlockYamlFactory extends YAMLFactory {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a factory from a builder carrying the YAML settings to use.
     *
     * @param b
     *            the builder, configured as it would be for a plain {@link YAMLFactory}
     */
    public FlockYamlFactory(YAMLFactoryBuilder b) {
        super(b);
    }

    /**
     * A builder carrying this library's defaults, which is the usual way to obtain a factory.
     *
     * @return a new builder
     */
    public static FlockYamlFactoryBuilder builder() {
        return new FlockYamlFactoryBuilder();
    }

    @Override
    protected YAMLGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        int feats = _yamlGeneratorFeatures;
        if (_dumperOptions == null) {
            return new FlockYamlGenerator(ctxt, _generatorFeatures, feats, _quotingChecker, _objectCodec, out, _version);
        } else {
            return new FlockYamlGenerator(ctxt, _generatorFeatures, feats, _quotingChecker, _objectCodec, out, _dumperOptions);
        }
    }
}
