package io.github.ctgnz.yamlflock;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Writes a value in YAML block style, one entry per line, instead of inline. The complement of {@link YamlFlowStyle}, and useful where a property's own type asks for flow style
 * but this one occurrence of it should not.
 * <p>
 * It can be declared two ways. On a <strong>field or accessor</strong>, it applies to that property:
 *
 * <pre>
 * &#64;YamlBlockStyle
 * public Map&lt;String, Edition&gt; getEditions() {
 *     return editions;
 * }
 * </pre>
 * <p>
 * On the <strong>type that owns the properties</strong>, {@link #properties()} names them. Use this form where the property cannot be annotated directly - it is inherited,
 * generated, or declared on a type you do not control:
 *
 * <pre>
 * &#64;YamlBlockStyle(properties = {"editions"})
 * public class Holdings { ... }
 * </pre>
 * <p>
 * Either way it applies to the annotated property and no deeper. Writing a map or a collection in block style does not make its entries block as well; those still take their style
 * from their own type, so a block map of flow-style values reads one entry per line with each entry inline:
 *
 * <pre>
 * editions:
 *   first: {format: HARDBACK, pages: 777, published: 1979}
 * </pre>
 * <p>
 * To reach deeper than one level, annotate the intervening type. Note also that this cannot override an enclosing {@link YamlFlowStyle}: YAML forbids block content inside flow
 * content, so within a flow-style value the annotation has no effect.
 *
 * @author ctg
 */
@Documented
@Retention(RUNTIME)
@Target({
    TYPE, FIELD, METHOD
})
public @interface YamlBlockStyle {

    /**
     * The names of the properties to write in block style, for the form declared on the owning type. Ignored where the annotation is on a field or accessor, which names its
     * property by where it sits.
     *
     * @return the property names, or empty where none are named
     */
    String[] properties() default {};

}
