Feature: Forcing quotes on a property

  With MINIMIZE_QUOTES enabled - which is what makes a document readable - YAML writes a string
  unquoted wherever it can. SnakeYAML is careful about this, and more careful than it first appears:
  a string whose characters resolve as a number is quoted anyway. So an unforced ISBN keeps its
  leading zero, and so does a shelving key; see the Author record, which is deliberately left
  unforced to make that point.

  What is at stake is therefore predictability rather than data. Whether a value is quoted depends
  on the value, so one record reads isbn: '0306406152' and the next isbn: 030640615X. Nor is the
  quote character settled: a value quoted because it resolves as a number gets single quotes, one
  quoted because it resolves as a boolean gets double, and a person hand-editing the file will type
  whichever they prefer. One column, three renderings, none of them wrong.

  For a document that lives in source control that matters more than appearance: the same field
  should be written the same way every time, or a diff shows churn that is not a change. Forcing
  the quotes on a field says "this one will need them sometimes, so use them always".

  An ISBN makes the mechanism easy to see, because whether a given one needs quoting turns on
  something as arbitrary as its check digit. A title is the stronger case, because the values that
  will need quoting cannot be enumerated in advance: a title may contain a colon, a leading at-sign,
  a comma that falls inside a flow mapping, or an apostrophe that rules out the single quotes the
  writer would otherwise reach for. Left unforced, such a column is quoted for most of a catalogue
  and bare for the rest, and which is which changes as the catalogue grows. Forced, it is settled
  once.

  Accented and non-Latin characters are not among the reasons, which is worth stating because it is
  the plausible-sounding one: the emitter allows Unicode, so those characters are written literally
  whether the property is forced or not. A title needs quotes for its punctuation, not its alphabet.

  The YamlForceQuote annotation names those properties, on the type that declares them.
  A line of a description cannot begin with an at-sign: Gherkin reads that as a tag.

  Scenario: an ISBN with a leading zero keeps its quotes and its zero
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      """

  Scenario: an unforced shelving key keeps its leading zeros too, by a different quote character
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it is by "Douglas Hofstadter" sorting as "0001" born 1945
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      authors:
      - {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
      """

  Scenario: a title carrying a reserved character is quoted, which is why the property is forced
    Given a book "0262510871" titled "Structure and Interpretation: 2nd Edition" published in 1985
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0262510871", title: "Structure and Interpretation: 2nd Edition", year: 1985}
      """

  Scenario: a title with an apostrophe is quoted, and accented characters are written as they are
    Given a book "2070293882" titled "L'Être et le Néant" published in 1943
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "2070293882", title: "L'Être et le Néant", year: 1943}
      """

  Scenario: a non-Latin title is written in its own script
    Given a book "4101001014" titled "雪国" published in 1947
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "4101001014", title: "雪国", year: 1947}
      """
