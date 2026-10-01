package io.github.ctgnz.yamlflock.catalog;

import io.github.ctgnz.yamlflock.YamlFlowStyle;

/**
 * A book's author, and the counter-example: the case that looks like it needs {@link io.github.ctgnz.yamlflock.YamlForceQuote} and does not.
 * <p>
 * Flow style, so each author is one line within a block list. {@code sortKey} is then the interesting part. A shelving key like {@code "0001"} is a string whose leading zeros
 * carry meaning, which reads as exactly the property a developer would reach for force quoting to protect - and it needs no such protection. SnakeYAML's quoting checker resolves
 * {@code 0001} as an integer and therefore quotes it on its own, so the value is written {@code sortKey: '0001'} and read back as the string it went out as. Forcing the quotes
 * here would buy nothing.
 * <p>
 * It is kept unforced deliberately, and not only to avoid a pointless annotation. Written beside a forced property it makes the quote character visible: a single author line
 * carries {@code isbn: "0306406152"} from {@link Book.Details}, which is forced, next to {@code sortKey: '0001'}, which is not. Both are quoted, by different characters, for the
 * same kind of value - which is the inconsistency force quoting exists to settle, shown rather than described.
 */
@YamlFlowStyle
public record Author(String name, String sortKey, int born) {
}
