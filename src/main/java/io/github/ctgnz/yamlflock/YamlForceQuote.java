package io.github.ctgnz.yamlflock;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Always writes the named properties quoted, whatever their value. Declared on the type that owns them, naming them in {@link #properties()}:
 *
 * <pre>
 * &#64;YamlForceQuote(properties = {
 *     "isbn", "title"
 * })
 * public record Details(String isbn, String title, int year) {
 * }
 * </pre>
 * <p>
 * This is about predictability rather than correctness. SnakeYAML quotes a value that would otherwise read back as a different type, so a numeric-looking string keeps its quotes
 * and its leading zeros without help. What it does not do is quote a property <em>consistently</em>: whether a given value is quoted depends on that value, and so does the quote
 * character, so one column can hold {@code '0306406152'}, {@code 030640615X} and {@code "no"} at once. For a document kept in source control that matters - a field written a
 * different way on each occurrence shows up in a diff as churn that is not a change, and a hand edit will use whichever quote character the editor preferred.
 * <p>
 * Reach for it on any property that will need quotes for some of its values: identifiers whose quoting depends on their digits, and free text, which can contain a colon, a leading
 * {@code @}, or a comma that falls inside a flow mapping. Accented and non-Latin characters are not a reason; those are written literally either way.
 * <p>
 * There is one case where it does affect correctness. The YAML float literals {@code .inf}, {@code -.inf} and {@code .nan} are not quoted automatically, so a string property
 * holding one of them is written bare and cannot be read back. Force the quotes on any property that may hold text from outside your control.
 *
 * @author ctg
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface YamlForceQuote {

    /**
     * The names of the properties to write quoted.
     *
     * @return the property names, or empty where none are named
     */
    String[] properties() default {};

}
