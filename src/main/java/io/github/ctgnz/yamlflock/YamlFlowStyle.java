package io.github.ctgnz.yamlflock;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Writes a value inline, as a YAML flow collection on one line, instead of one entry per line.
 * <p>
 * On a <strong>type</strong>, every value of that type is written inline wherever it appears:
 *
 * <pre>
 * &#64;YamlFlowStyle
 * public record Edition(Format format, int pages, int published) {}
 *
 * first: {format: HARDBACK, pages: 777, published: 1979}
 * </pre>
 * <p>
 * On a <strong>field or accessor</strong>, that one property is written inline whatever its type. This is the only way to inline a collection, a {@code List<Genre>} having no type
 * of its own to annotate:
 *
 * <pre>
 * &#64;YamlFlowStyle
 * public List&lt;Genre&gt; getGenres() { return genres; }
 *
 * genres: [PHILOSOPHY, MATHEMATICS]
 * </pre>
 * <p>
 * YAML allows flow content inside block content but not the reverse, so nothing nested within a flow-style value can be written in block style. A {@link YamlBlockStyle} on a
 * property of a flow-style type therefore has <em>no effect</em> - the value is still written inline, because the format cannot express the alternative. To place inline scalars
 * beside a block-style map, give the scalars their own flow-style type and leave the map a sibling of it rather than annotating the enclosing class.
 *
 * @author ctg
 */
@Documented
@Retention(RUNTIME)
@Target({
    TYPE, FIELD, METHOD
})
public @interface YamlFlowStyle {

}
