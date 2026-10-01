package io.github.ctgnz.yamlflock;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.LoaderOptions;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;

/**
 * Builds a {@link FlockYamlFactory} already configured the way this library needs, so that the common case is one line:
 *
 * <pre>
 * ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().build());
 * </pre>
 * <p>
 * The settings a caller does choose are named directly - {@link #lineWidth(int)}, {@link #lineBreak(LineBreak)}, {@link #defaultFlowStyle(FlowStyle)},
 * {@link #documentStartMarker(boolean)}, {@link #minimizeQuotes(boolean)} and {@link #codePointLimit(int)} - so refining one of them does not mean taking ownership of all of them:
 *
 * <pre>
 * ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().lineBreak(LineBreak.UNIX).build());
 * </pre>
 *
 * <h2>The defaults</h2>
 * <table border="1">
 * <caption>What a freshly created builder carries</caption>
 * <tr>
 * <th>setting</th>
 * <th>value</th>
 * <th>why</th>
 * </tr>
 * <tr>
 * <td>default flow style</td>
 * <td>BLOCK</td>
 * <td>the fallback for anything the annotations do not inline</td>
 * </tr>
 * <tr>
 * <td>line width</td>
 * <td>480</td>
 * <td>SnakeYAML folds a flow collection wider than this, which reintroduces the unpredictability the annotations exist to remove</td>
 * </tr>
 * <tr>
 * <td>line break</td>
 * <td>WIN</td>
 * <td>see below</td>
 * </tr>
 * <tr>
 * <td>pretty flow</td>
 * <td>false</td>
 * <td>keeps an inlined collection on one line</td>
 * </tr>
 * <tr>
 * <td>canonical</td>
 * <td>false</td>
 * <td>canonical output tags and quotes everything</td>
 * </tr>
 * <tr>
 * <td>MINIMIZE_QUOTES</td>
 * <td>enabled</td>
 * <td>without it every scalar is double-quoted and inline output is not worth reading</td>
 * </tr>
 * <tr>
 * <td>WRITE_DOC_START_MARKER</td>
 * <td>disabled</td>
 * <td>no leading marker on a configuration file</td>
 * </tr>
 * <tr>
 * <td>code point limit</td>
 * <td>16 MiB</td>
 * <td>SnakeYAML's own 3 MiB default refuses to read a large document</td>
 * </tr>
 * </table>
 * <p>
 * The line break is the one default worth explaining, being the opposite of what a library default usually is. These files are generated, committed, then hand-edited, and a line
 * break that disagrees with what is already on disk rewrites every line of every file the first time anything is written - exactly the diff churn this library exists to avoid.
 * Pass {@link #lineBreak(LineBreak)} where that is not wanted.
 * <h2>Going deeper</h2> Everything {@link YAMLFactoryBuilder} offers still works, and the methods here return this type, so a chain keeps reaching the named settings above however
 * it is ordered. Nothing is validated: {@link #dumperOptions(DumperOptions)} and {@link #loaderOptions(LoaderOptions)} each replace a whole options object, so calling either
 * discards any named setting applied to it beforehand. The last call wins, and a caller who reaches for the underlying options owns the result.
 *
 * @author ctg
 */
public class FlockYamlFactoryBuilder extends YAMLFactoryBuilder {

    /** The default width in characters, past which SnakeYAML folds a flow collection onto another line. */
    public static final int DEFAULT_LINE_WIDTH = 480;

    /** The default limit on the size of a document that may be read, 16 MiB, in place of SnakeYAML's 3 MiB. */
    public static final int DEFAULT_CODE_POINT_LIMIT = 16 * 1024 * 1024;

    /**
     * Creates a builder carrying the defaults described above.
     * <p>
     * The inherited fields are assigned directly rather than through this class's own setters, which a constructor must not call while the instance is still being built.
     */
    protected FlockYamlFactoryBuilder() {
        DumperOptions dumper = new DumperOptions();
        dumper.setDefaultFlowStyle(FlowStyle.BLOCK);
        dumper.setPrettyFlow(false);
        dumper.setCanonical(false);
        dumper.setWidth(DEFAULT_LINE_WIDTH);
        dumper.setLineBreak(LineBreak.WIN);
        _dumperOptions = dumper;

        LoaderOptions loader = new LoaderOptions();
        loader.setCodePointLimit(DEFAULT_CODE_POINT_LIMIT);
        _loaderOptions = loader;

        _formatGeneratorFeatures |= YAMLGenerator.Feature.MINIMIZE_QUOTES.getMask();
        _formatGeneratorFeatures &= ~YAMLGenerator.Feature.WRITE_DOC_START_MARKER.getMask();
    }

    /**
     * Sets the width past which a flow collection is folded onto another line.
     *
     * @param width
     *            the width in characters
     * @return this builder
     */
    public FlockYamlFactoryBuilder lineWidth(int width) {
        dumper().setWidth(width);
        return this;
    }

    /**
     * Sets the line break written between lines.
     *
     * @param lineBreak
     *            the line break to write
     * @return this builder
     */
    public FlockYamlFactoryBuilder lineBreak(LineBreak lineBreak) {
        dumper().setLineBreak(lineBreak);
        return this;
    }

    /**
     * Sets the style used for anything the annotations leave undecided.
     *
     * @param style
     *            the fallback style
     * @return this builder
     */
    public FlockYamlFactoryBuilder defaultFlowStyle(FlowStyle style) {
        dumper().setDefaultFlowStyle(style);
        return this;
    }

    /**
     * Sets whether a document opens with a start marker.
     *
     * @param write
     *            true to write the marker
     * @return this builder
     */
    public FlockYamlFactoryBuilder documentStartMarker(boolean write) {
        return configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, write);
    }

    /**
     * Sets whether a scalar is left unquoted wherever it can be.
     *
     * @param minimize
     *            true to quote only where a value needs it
     * @return this builder
     */
    public FlockYamlFactoryBuilder minimizeQuotes(boolean minimize) {
        return configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, minimize);
    }

    /**
     * Sets the largest document that may be read.
     *
     * @param limit
     *            the limit in code points
     * @return this builder
     */
    public FlockYamlFactoryBuilder codePointLimit(int limit) {
        loader().setCodePointLimit(limit);
        return this;
    }

    /** The options the named setters write into, created if {@link #dumperOptions(DumperOptions)} was passed null. */
    private DumperOptions dumper() {
        if (_dumperOptions == null) {
            _dumperOptions = new DumperOptions();
        }
        return _dumperOptions;
    }

    /** The options the named setters write into, created if {@link #loaderOptions(LoaderOptions)} was passed null. */
    private LoaderOptions loader() {
        if (_loaderOptions == null) {
            _loaderOptions = new LoaderOptions();
        }
        return _loaderOptions;
    }

    @Override
    public FlockYamlFactory build() {
        return new FlockYamlFactory(this);
    }

    @Override
    public FlockYamlFactoryBuilder enable(YAMLParser.Feature f) {
        super.enable(f);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder enable(YAMLParser.Feature first, YAMLParser.Feature... other) {
        super.enable(first, other);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder disable(YAMLParser.Feature f) {
        super.disable(f);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder disable(YAMLParser.Feature first, YAMLParser.Feature... other) {
        super.disable(first, other);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder configure(YAMLParser.Feature f, boolean state) {
        super.configure(f, state);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder enable(YAMLGenerator.Feature f) {
        super.enable(f);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder enable(YAMLGenerator.Feature first, YAMLGenerator.Feature... other) {
        super.enable(first, other);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder disable(YAMLGenerator.Feature f) {
        super.disable(f);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder disable(YAMLGenerator.Feature first, YAMLGenerator.Feature... other) {
        super.disable(first, other);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder configure(YAMLGenerator.Feature f, boolean state) {
        super.configure(f, state);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder stringQuotingChecker(StringQuotingChecker sqc) {
        super.stringQuotingChecker(sqc);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder yamlVersionToWrite(DumperOptions.Version v) {
        super.yamlVersionToWrite(v);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder loaderOptions(LoaderOptions loaderOptions) {
        super.loaderOptions(loaderOptions);
        return this;
    }

    @Override
    public FlockYamlFactoryBuilder dumperOptions(DumperOptions dumperOptions) {
        super.dumperOptions(dumperOptions);
        return this;
    }

}
