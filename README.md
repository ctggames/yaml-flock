# yaml-flock

A Jackson extension that lets a single YAML document mix **fl**ow and bl**ock** style, chosen property by property, by annotating the model rather than configuring the writer.

```yaml
details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
genres: [PHILOSOPHY, MATHEMATICS]
authors:
- {name: Douglas Hofstadter, sortKey: "0001", born: 1945}
editions:
  first: {format: HARDBACK, pages: 777, published: 1979}
  anniversary: {format: PAPERBACK, pages: 824, published: 1999}
```

## Why

YAML is most often used for files that live in source control. That is the job it is good at, and the reason it displaced JSON for the purpose: it dispenses with a lot of furniture - braces, brackets, quotes around every key, commas that a diff will flag on the line *above* the one that actually changed - which makes JSON far too verbose to read or review as a configuration file.

Standard block style still carries a cost, though. One property per line is exactly the shape you want for something like a Spring `application.yml`, where each line is a single variable and each variable deserves a line. But as soon as the data has real structure, the one-property-per-line rule turns a screenful of file into mostly blank space. The same book, written in plain block style:

```yaml
details:
  isbn: "0306406152"
  title: "Godel Escher Bach"
  year: 1979
genres:
- "PHILOSOPHY"
- "MATHEMATICS"
authors:
- name: "Douglas Hofstadter"
  sortKey: "0001"
  born: 1945
editions:
  first:
    format: "HARDBACK"
    pages: 777
    published: 1979
  anniversary:
    format: "PAPERBACK"
    pages: 824
    published: 1999
```

Twenty lines instead of seven, and not one of them tells you anything the shorter version didn't. Scale that up and you are scrolling through structure rather than reading data: a three-book catalogue goes from 52 lines to 21.

Flow style alone is not the answer either - collapse the whole document and you get long lines that a diff reports as one wholesale change, which is exactly the property that made JSON unpleasant to review.

Flock style is the middle ground. Values that belong together on one line go on one line; structure that deserves a line per entry keeps it. The result uses the screen efficiently while staying predictable and well-structured, so it diffs cleanly: a changed page count is a changed word on one line, and an added edition is one added line.

The emphasis on *predictable* is deliberate. A file under source control is written by a program and then edited by hand, repeatedly, and the two have to agree on the shape of the output or every regeneration produces a diff full of noise nobody meant to review. That is what the annotations are for - the developer states the intended shape once, on the model, and every write produces it.

## Installing

```xml
<dependency>
    <groupId>io.github.ctgnz</groupId>
    <artifactId>yaml-flock</artifactId>
    <version>1.0.0</version>
</dependency>
```

Requires Java 25 and Jackson 2.22. The only dependencies are `jackson-databind` and `jackson-dataformat-yaml`.

## Wiring it up

```java
ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder().build());
mapper.setDefaultPropertyInclusion(Include.NON_DEFAULT);
```

That is the whole of it. `FlockYamlFactory.builder()` arrives carrying the settings this library needs, so there is nothing you are obliged to remember:

| setting | default | why it is the default |
| --- | --- | --- |
| default flow style | `BLOCK` | the fallback for anything the annotations do not inline |
| line width | 480 | SnakeYAML folds a flow collection wider than this, which reintroduces exactly the unpredictability the annotations exist to remove |
| line break | `UNIX` | see below |
| pretty flow | `false` | keeps an inlined collection on one line |
| canonical | `false` | canonical output tags and quotes everything |
| `MINIMIZE_QUOTES` | enabled | without it every scalar is double-quoted, and an inlined map spends more characters on quotes than on content |
| `WRITE_DOC_START_MARKER` | disabled | no leading `---` on a configuration file |
| code point limit | 16 MiB | SnakeYAML's own 3 MiB default refuses to read a large document |

**The line break is worth a word.** The default is LF, because wanting CRLF is a property of a project whose files are already committed with CRLF rather than a property of YAML. If that is your situation, say so — a line break disagreeing with what is on disk rewrites every line of every file the first time anything is written, which is the exact churn this library exists to avoid:

```java
ObjectMapper mapper = new ObjectMapper(FlockYamlFactory.builder()
    .lineBreak(LineBreak.WIN)
    .build());
```

The named settings — `lineWidth`, `lineBreak`, `defaultFlowStyle`, `documentStartMarker`, `minimizeQuotes`, `codePointLimit` — refine one thing each, so changing one does not mean taking ownership of all of them. Everything `YAMLFactoryBuilder` offers still works too, and these methods return the Flock builder, so the chain keeps reaching them however you order it.

Nothing is validated. `dumperOptions(...)` and `loaderOptions(...)` each replace a whole options object, so calling either discards any named setting applied to it beforehand — last call wins. Reach for them and you own the result.

`Include.NON_DEFAULT` is a choice about your model rather than about YAML, so it stays on the mapper and out of the builder.

Reading is unchanged: YAML style is a presentation choice, so a plain `YAMLFactory` parses anything this writes. Only the writing side needs the extension.

## The annotations

### `@YamlFlowStyle`

Write this on one line. Allowed on a type, a field, or an accessor.

On a **type**, every value of that type is inlined wherever it appears:

```java
@YamlFlowStyle
public record Edition(Format format, int pages, int published) {}
```

On a **member**, the property is inlined whatever its type - which is the only way to say it when there is no type to annotate. Nobody can put an annotation on `List<Genre>`, and annotating `Genre` itself would make a statement about the enum rather than about this one property:

```java
@YamlFlowStyle
public List<Genre> getGenres() {
    return genres;
}
```

```yaml
genres: [PHILOSOPHY, MATHEMATICS]
```

### `@YamlBlockStyle`

Write this one entry per line - the override, for a property whose own type is flow style, or whose owner is. Allowed on a type, a field, or an accessor, and additionally on the owning type naming properties by name:

```java
@YamlBlockStyle(properties = {"editions"})
public class Holdings { ... }
```

The name form exists because the property may be declared somewhere you cannot annotate - inherited, generated, or on a type you do not own. Where you can reach the member, annotate the member; it keeps the statement next to the thing it describes.

The override applies **one level only**, not transitively. Marking a map block style makes the map a block mapping; its entries still take their style from their own type, which is usually the point:

```yaml
editions:
  first: {format: HARDBACK, pages: 777, published: 1979}
```

### `@YamlForceQuote`

Always quote this property's values, whether or not the particular value needs it. Type-level, naming properties:

```java
@YamlFlowStyle
@YamlForceQuote(properties = {"isbn", "title"})
public record Details(String isbn, String title, int year) {}
```

Mostly this is not protection against data loss. SnakeYAML's quoting checker is more careful than it looks: a string whose characters resolve as a number gets quoted on its own, so an unforced ISBN keeps both its quotes and its leading zero. The catalogue keeps `Author.sortKey` unforced on purpose to make that point - a shelving key of `"0001"` reads as exactly the property you would reach for this annotation to protect, and it needs no protection.

What the annotation buys is *predictability*, and the sortKey counter-example is also the clearest demonstration of why that is worth something. One author line carries both:

```yaml
details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
authors:
- {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
```

Two values of the same kind, both quoted, by different characters - because `isbn` is forced and `sortKey` is quoted by the checker, which reaches for `'` where the forced path uses `"`. Unforced, a column's rendering is decided per value: `isbn: '0306406152'` where the digits resolve as a number, `isbn: 030640615X` where they do not, and whichever quote character a person typed where the file was last hand-edited. One column, three renderings, none of them wrong, all of them diff noise.

`title` is the stronger case than `isbn`, because the values that need quoting cannot be enumerated in advance: a title may contain a colon, a leading `@`, a comma that falls inside a flow mapping, or an apostrophe - which rules out the single quotes the writer would otherwise reach for, so `L'Être et le Néant` comes out bare while `Structure and Interpretation: 2nd Edition` comes out double-quoted. Forced, both are double-quoted and the column is settled.

One genuine correctness case does exist, and it is worth knowing. The checker does **not** quote the YAML float literals `.inf`, `-.inf` and `.nan`, so a string property holding one of those is written bare and then **fails to read back at all** - Jackson reports `Malformed numeric value '.inf'`. Forcing the quotes fixes it. That is a narrow class, but if a property can hold arbitrary text from outside your control, force it.

Accented and non-Latin characters are *not* a reason, which is worth saying because it is the plausible-sounding one. The emitter allows Unicode, so `L'Être et le Néant` and `雪国` are written literally whether the property is forced or not. A title needs quotes for its punctuation, not its alphabet.

So reach for it on any property that will need quotes for *some* of its values - identifiers whose quoting turns on a check digit, free text in any language, anything that can hold `y`, `n`, `on`, `off` or `.inf` - and settle the question once rather than per value.

## How a style is resolved

For each value written, the first of these that applies wins:

1. **`@YamlFlowStyle` or `@YamlBlockStyle` on the member** declaring this property - its field, or its `get`/`is` accessor, searched up the superclass chain.
2. **`@YamlBlockStyle(properties = ...)` on the owning type**, naming this property.
3. **A block-style override on the enclosing container**, for the immediate contents of a collection that was itself forced block.
4. **`@YamlFlowStyle` on the value's own type.**
5. **Block style**, which is both the default and what a plain `YAMLFactory` would have done - so an unannotated model is written exactly as it is today, and the extension is inert until something asks for flow.

## The one rule YAML imposes

**Flow content may contain only flow content.** A block mapping may hold flow collections; a flow collection may not hold block ones. This is the format's rule rather than a limitation of this library, and no amount of annotation gets round it - a document that breaks it does not parse.

The practical consequence is worth knowing before designing a model around it. This does **not** work - and it fails quietly, which is the part to watch for. The block override is ignored and the whole object comes out inline, because the emitter has no way to express the alternative:

```java
@YamlFlowStyle                                      // the whole class inlined ...
public class Book {
    @YamlBlockStyle                                 // ... except this? No.
    public Map<String, Edition> getEditions() { ... }
}
```

To get scalars on one line *beside* a block-style map - the shape at the top of this README - the scalars go in a nested flow-style record, and the block map stays a sibling at the parent's level:

```java
public class Book {                                 // block, so it may contain flow

    @YamlFlowStyle
    public record Details(String isbn, String title, int year) {}

    @JsonGetter("details")
    Details getDetails() { ... }                    // one line

    public Map<String, Edition> getEditions() { ... }   // block, entries flow
}
```

That nested record is not a workaround for a missing feature. It is the shape the format permits, and the library's job is to make it expressible rather than to pretend the constraint is not there.

## Design notes

Context that is not needed to use the library, kept here rather than in the javadoc.

**Why annotations rather than a pretty printer.** The obvious place to put these decisions is a `DefaultPrettyPrinter`, and both projects this emitter came from had one. It never ran. `DefaultPrettyPrinter` is consulted for JSON output only - `YAMLGenerator` emits through SnakeYAML and never asks it anything - so the printer's two arrays of property names, one for maps and one for objects, had no effect on either project's output. Emptying them left both byte-identical. The decisions had to move to where the generator could actually see them, and an annotation on the model is a better home for them anyway: the shape of the file is stated next to the thing whose shape it is, and it travels with the model rather than with the writer.

That also means the semantics here are defined by this project's own scenarios rather than inherited from prior behaviour. There was no prior behaviour to preserve.

**Why `properties()` is scanned rather than searched.** The name lists are a handful of entries each, so a loop beats building a `Set`, and it keeps the dependency list at two - `jackson-databind` and `jackson-dataformat-yaml`. A published library should not make its consumers reconcile a transitive commons-lang3 to search an array of strings.

**Why member-level annotations exist at all.** A type-level annotation cannot say anything about a `List<Genre>`: there is no type to annotate, and annotating `Genre` would make a statement about the enum everywhere it appears. Allowing the annotations on a field or accessor is what makes a collection's style expressible without reshaping the model to suit the file.

## The specification

The behaviour above is specified by the Cucumber scenarios in [`src/test/resources/features`](src/test/resources/features), written against a library-catalogue model in [`src/test/java/io/github/ctgnz/yamlflock/catalog`](src/test/java/io/github/ctgnz/yamlflock/catalog). Each scenario asserts a complete document, so they read as worked examples rather than as assertions about internals:

| Feature | Covers |
| --- | --- |
| `flow-style.feature` | `@YamlFlowStyle` on a type, and the unannotated default |
| `block-style.feature` | the override on a type, on a member, and on a map's entries |
| `collections.feature` | lists of enums, and lists of objects |
| `force-quote.feature` | leading zeros, reserved characters, apostrophes, non-Latin scripts, and the unforced counter-example |
| `nesting.feature` | the flow-record pattern, and one document using every style at once |

They are the specification in a real sense: this emitter was extracted from two projects that had never exercised the behaviour, so there was no prior output to preserve. The features say what the library does, and the generator is written to satisfy them.

## Licence

Apache License 2.0. See [LICENSE](LICENSE).
