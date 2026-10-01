package io.github.ctgnz.yamlflock.fixture;

import java.util.ArrayList;
import java.util.List;

import io.github.ctgnz.yamlflock.catalog.ArchiveEntry;
import io.github.ctgnz.yamlflock.catalog.Author;
import io.github.ctgnz.yamlflock.catalog.Book;
import io.github.ctgnz.yamlflock.catalog.Catalog;
import io.github.ctgnz.yamlflock.catalog.Edition;
import io.github.ctgnz.yamlflock.catalog.Format;
import io.github.ctgnz.yamlflock.catalog.Genre;
import io.github.ctgnz.yamlflock.catalog.SearchIndex;

/**
 * Builds the catalogue behind the committed fixture, deterministically.
 * <p>
 * Nothing here is random, and nothing is derived from the clock or the platform: the same call produces the same catalogue on any machine, which is what lets the fixture be a
 * byte-exact baseline rather than a snapshot of one run.
 * <p>
 * <strong>Every value is non-default on purpose.</strong> The mapper writes with {@code NON_DEFAULT}, so a zero {@code int}, an empty collection or a null would simply be omitted
 * and the document would come back shorter than the object that produced it - a round-trip failure with nothing to do with styling. So no year, page count or birth year is zero,
 * and no collection is left empty.
 */
public final class CatalogFixture {

    /** How many books the fixture carries. Large enough that the document is a realistic file rather than an example. */
    public static final int BOOKS = 100;

    private static final String[] TITLE_HEADS = {
        "Structure and Interpretation", "The Histories", "Godel Escher Bach", "A Pattern Language", "The Art of Memory", "Laws of Form", "The Mezzanine", "Pale Fire", "Labyrinths",
        "The Rings of Saturn"
    };

    /** Titles chosen to make the forced quoting on {@code title} do visible work: a colon, an apostrophe, a leading at-sign, accents, a non-Latin script. */
    private static final String[] TITLE_TAILS = {
        ": An Eternal Golden Braid", " and Other Writings", ": Volume II", "", " (Revised)", ": L'Etre et le Neant", " @ the Edge", ": Gedel, Escher, Bach", " - 雪国", ": A Second Look"
    };

    private static final String[] AUTHOR_NAMES = {
        "Douglas Hofstadter", "Herodotus", "Christopher Alexander", "Frances Yates", "George Spencer-Brown", "Nicholson Baker", "Vladimir Nabokov", "Jorge Luis Borges", "W G Sebald", "Harold Abelson"
    };

    private CatalogFixture() {
    }

    /** The catalogue the fixture is written from. */
    public static Catalog catalogue() {
        Catalog catalog = new Catalog("Ctgnz Reference Library", 1987).foundedOn(new Edition(Format.HARDBACK, 412, 1987));

        List<Book> books = new ArrayList<>();
        for (int i = 0; i < BOOKS; i++) {
            books.add(book(i));
        }
        catalog.holding(books.toArray(new Book[0]));

        List<ArchiveEntry> archive = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            archive.add(new ArchiveEntry(shelfMark(i), new Edition(format(i), 120 + i * 37, 1890 + i * 7)));
        }
        catalog.stacked(archive.toArray(new ArchiveEntry[0]));

        catalog.indexed(index());
        return catalog;
    }

    private static Book book(int i) {
        Book book = new Book(isbn(i), title(i), 1890 + (i * 7) % 130);
        book.with(genres(i));
        book.by(authors(i));
        book.printed("first", new Edition(format(i), 180 + (i * 13) % 700, 1890 + (i * 7) % 130));
        if (i % 2 == 0) {
            book.printed("reprint", new Edition(format(i + 1), 190 + (i * 11) % 700, 1950 + (i * 3) % 70));
        }
        return book;
    }

    /**
     * An ISBN per book, half of them with a leading zero and every tenth ending in X.
     * <p>
     * That mixture is the point: an unforced column would quote the leading-zero ones and leave the rest bare, which is what {@code YamlForceQuote} on this property settles.
     */
    private static String isbn(int i) {
        String digits = String.format("%09d", (i * 7919) % 1_000_000_000);
        return (i % 10 == 9) ? digits + "X" : "0" + digits.substring(1) + (i % 7);
    }

    private static String title(int i) {
        return TITLE_HEADS[i % TITLE_HEADS.length] + TITLE_TAILS[(i / TITLE_HEADS.length) % TITLE_TAILS.length];
    }

    private static Genre[] genres(int i) {
        Genre[] all = Genre.values();
        int count = 1 + i % 3;
        Genre[] chosen = new Genre[count];
        for (int n = 0; n < count; n++) {
            chosen[n] = all[(i + n) % all.length];
        }
        return chosen;
    }

    private static Author[] authors(int i) {
        int count = 1 + i % 2;
        Author[] chosen = new Author[count];
        for (int n = 0; n < count; n++) {
            int which = (i + n) % AUTHOR_NAMES.length;
            chosen[n] = new Author(AUTHOR_NAMES[which], String.format("%04d", 1 + which + n * 50), 1890 + (which * 11) % 100);
        }
        return chosen;
    }

    private static Format format(int i) {
        return Format.values()[i % Format.values().length];
    }

    private static String shelfMark(int i) {
        return String.format("%03d", 1 + i * 11);
    }

    private static SearchIndex index() {
        SearchIndex index = new SearchIndex();
        index.title("structure", isbn(0), isbn(10), isbn(20));
        index.title("histories", isbn(1), isbn(11));
        index.author("hofstadter", isbn(0), isbn(30));
        index.author("borges", isbn(7), isbn(17), isbn(27));
        index.year(1890, isbn(0));
        index.year(1897, isbn(1), isbn(2));
        return index;
    }

}
