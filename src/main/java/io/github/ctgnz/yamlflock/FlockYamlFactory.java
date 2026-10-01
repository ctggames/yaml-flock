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
 * DumperOptions options = new DumperOptions();
 * options.setDefaultFlowStyle(FlowStyle.BLOCK);
 * options.setWidth(480);
 *
 * YAMLFactory factory = new FlockYamlFactory(YAMLFactory.builder().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES).dumperOptions(options));
 *
 * ObjectMapper mapper = new ObjectMapper(factory);
 * </pre>
 * <p>
 * Two settings are worth attention. {@code MINIMIZE_QUOTES} is what makes inline output worth reading, since without it every scalar is double-quoted. A generous {@code setWidth}
 * matters because SnakeYAML folds a flow collection that exceeds the width, which reintroduces the unpredictability the annotations exist to remove - set it wider than the longest
 * line intended.
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
