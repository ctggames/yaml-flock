package io.github.ctgnz.yamlflock.catalog;

import io.github.ctgnz.yamlflock.YamlBlockStyle;
import io.github.ctgnz.yamlflock.YamlFlowStyle;

/**
 * One printing of a book.
 * <p>
 * Flow style because an edition is three small facts that read better on one line, and because a catalogue holds a map of them - a block-style edition costs four lines apiece.
 * That decision belongs to the type here, which is the point {@link ArchiveEntry} exists to complicate: an owner can override it per property with {@link YamlBlockStyle}.
 */
@YamlFlowStyle
public record Edition(Format format, int pages, int published) {
}
