package io.github.ctgnz.yamlflock.catalog;

import io.github.ctgnz.yamlflock.YamlBlockStyle;
import io.github.ctgnz.yamlflock.YamlFlowStyle;

/**
 * A single volume in the closed stacks, and the reason {@link YamlBlockStyle} exists.
 * <p>
 * It holds the same {@link Edition} type that {@link Book} does, but renders it differently: {@code Book} takes the flow style {@code Edition} declares for itself, while an
 * archive record spells the edition out one field per line, because an archivist reads it as a description rather than as a row in a table.
 * <p>
 * Nothing about {@code Edition} changes. The shape is the owner's decision, which is the whole argument for annotating the property rather than the type - a type-level
 * {@link YamlFlowStyle} is a default, not a verdict.
 */
@YamlBlockStyle(properties = {
    "edition"
})
public record ArchiveEntry(String shelfMark, Edition edition) {
}
