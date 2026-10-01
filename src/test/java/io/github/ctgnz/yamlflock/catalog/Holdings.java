package io.github.ctgnz.yamlflock.catalog;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.ctgnz.yamlflock.YamlBlockStyle;

/**
 * What the library physically holds of one title, shelf mark and all printings.
 * <p>
 * The same {@link Edition} map that a {@link Book} writes inline, written one field per line instead: a holdings record is a physical inventory that a person checks off against
 * the shelf, not a summary. So the owner overrides the type, as {@link ArchiveEntry} does - the difference being that here the property is a map, and the override reaches its
 * entries rather than the map itself, which was already block.
 */
@YamlBlockStyle(properties = {
    "editions"
})
public class Holdings {

    private String shelfMark;
    private final Map<String, Edition> editions = new LinkedHashMap<>();

    public Holdings() {
    }

    public Holdings(String shelfMark) {
        this.shelfMark = shelfMark;
    }

    public Holdings printed(String name, Edition edition) {
        editions.put(name, edition);
        return this;
    }

    public String getShelfMark() {
        return shelfMark;
    }

    public Map<String, Edition> getEditions() {
        return editions;
    }

}
