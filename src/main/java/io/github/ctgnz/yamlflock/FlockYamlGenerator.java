package io.github.ctgnz.yamlflock;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.Version;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;

/**
 * The generator behind {@link FlockYamlFactory}, which decides flow or block style per value from the annotations on the model. Obtained from the factory rather than constructed
 * directly; see {@link FlockYamlFactory} for how to set one up.
 *
 * @author ctg
 */
public class FlockYamlGenerator extends YAMLGenerator {

    public FlockYamlGenerator(IOContext ctxt, int jsonFeatures, int yamlFeatures, StringQuotingChecker quotingChecker, ObjectCodec codec, Writer out, DumperOptions dumperOptions) throws IOException {
        super(ctxt, jsonFeatures, yamlFeatures, quotingChecker, codec, out, dumperOptions);
    }

    public FlockYamlGenerator(IOContext ctxt, int jsonFeatures, int yamlFeatures, StringQuotingChecker quotingChecker, ObjectCodec codec, Writer out, Version version) throws IOException {
        super(ctxt, jsonFeatures, yamlFeatures, quotingChecker, codec, out, version);
    }

    @Override
    public void writeStartObject(Object forValue) throws IOException {
        _outputOptions.setDefaultFlowStyle(styleFor(forValue));
        super.writeStartObject(forValue);
    }

    /**
     * Block unless something asks otherwise, in order of who is most specific.
     * <p>
     * The member that declares the property wins, because that is the most local statement anyone made. Failing that, the type that owns the property may name it in a
     * {@link YamlBlockStyle} list. Failing that, the value's own type may ask for flow with {@link YamlFlowStyle}. Failing all three, block.
     * <p>
     * YAML permits flow content inside block but not block inside flow, so a block override has no effect once a flow style is already in force - the emitter cannot express it. A
     * class wanting its scalars on one line beside a block map therefore nests them in a flow-style record rather than annotating itself.
     */
    private FlowStyle styleFor(Object forValue) {
        FlowStyle declared = declaredStyle();
        if (declared != null) {
            return declared;
        }
        return forValue.getClass().getAnnotation(YamlFlowStyle.class) != null ? FlowStyle.FLOW : FlowStyle.BLOCK;
    }

    /**
     * The style the property being written was declared with, or null where nothing said.
     * <p>
     * Shared by mappings and sequences: a list has no type of its own to annotate - nobody can put an annotation on {@code List<Genre>} - so for a sequence this is the only place
     * a style can come from, and it is why {@link YamlFlowStyle} is allowed on a member at all.
     */
    private FlowStyle declaredStyle() {
        Object owner = _writeContext.getCurrentValue();
        String property = _writeContext.getCurrentName();
        if (owner != null) {
            Class<?> type = owner.getClass();
            if (MemberStyle.on(type, property, YamlFlowStyle.class) != null) {
                return FlowStyle.FLOW;
            }
            if (MemberStyle.on(type, property, YamlBlockStyle.class) != null || namedByOwner(_writeContext)) {
                return FlowStyle.BLOCK;
            }
        }
        return containerOwnerForcesBlock() ? FlowStyle.BLOCK : null;
    }

    /**
     * Whether the value sits inside a container whose own declaration asked for block.
     * <p>
     * A map or a collection carries no annotations and nobody declared it, so when a value is written inside one the question is put to whatever declared the container. That is
     * what lets an override on a map property reach the map's entries, the map itself having been block already. One level only: an owner states how its own property reads and has
     * no business reaching further, so anything deeper annotates the intermediate type instead.
     */
    private boolean containerOwnerForcesBlock() {
        Object owner = _writeContext.getCurrentValue();
        if (!(owner instanceof Map || owner instanceof Collection)) {
            return false;
        }
        JsonStreamContext parent = _writeContext.getParent();
        if (parent == null) {
            return false;
        }
        Object declaring = parent.getCurrentValue();
        return declaring != null && (MemberStyle.on(declaring.getClass(), parent.getCurrentName(), YamlBlockStyle.class) != null || namedByOwner(parent));
    }

    /** Whether the property named by the context is listed in a class-level {@link YamlBlockStyle} on the value that owns it. */
    private static boolean namedByOwner(JsonStreamContext context) {
        if (context == null) {
            return false;
        }
        Object owner = context.getCurrentValue();
        if (owner == null) {
            return false;
        }
        YamlBlockStyle annotation = owner.getClass().getAnnotation(YamlBlockStyle.class);
        return annotation != null && names(annotation.properties(), context.getCurrentName());
    }

    @Override
    public void writeStartArray(Object forValue, int size) throws IOException {
        FlowStyle declared = declaredStyle();
        _outputOptions.setDefaultFlowStyle(declared != null ? declared : FlowStyle.BLOCK);
        super.writeStartArray(forValue, size);
    }

    /** Whether {@code properties} names {@code property}. The arrays are a handful of entries, so a scan beats building a set. */
    static boolean names(String[] properties, String property) {
        if (property == null) {
            return false;
        }
        for (String candidate : properties) {
            if (property.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeString(String text) throws IOException, JsonGenerationException {
        if (!forcesQuotes()) {
            super.writeString(text);
            return;
        }
        boolean minimizing = isEnabled(Feature.MINIMIZE_QUOTES);
        disable(Feature.MINIMIZE_QUOTES);
        try {
            super.writeString(text);
        } finally {
            configure(Feature.MINIMIZE_QUOTES, minimizing);
        }
    }

    /**
     * Whether the property now being written is named by a {@link YamlForceQuote} on the type that owns it.
     * <p>
     * False where nothing owns it. A scalar written at the root of a document has no owning object - {@code mapper.writeValueAsString("hello")} is the plain case - and a value
     * with no owner cannot carry a property-level annotation, so there is nothing to look up and the value is written exactly as a plain {@code YAMLFactory} would write it.
     */
    private boolean forcesQuotes() {
        Object owner = _writeContext.getCurrentValue();
        if (owner == null) {
            return false;
        }
        YamlForceQuote annotation = owner.getClass().getAnnotation(YamlForceQuote.class);
        return annotation != null && names(annotation.properties(), _writeContext.getCurrentName());
    }

}
