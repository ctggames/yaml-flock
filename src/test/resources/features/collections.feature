Feature: Writing collections

  A list of scalars and a list of records are different things to read. A set of genres is a
  handful of values that belong on one line; a list of authors is a sequence of records that wants
  a line each. The library decides that from what the list holds, not from how it was declared.

  Scenario: a list of enums on a plain property is written inline
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it has genres "PHILOSOPHY, MATHEMATICS"
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      genres: [PHILOSOPHY, MATHEMATICS]
      """

  Scenario: a list of records is written one per line
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it is by "Douglas Hofstadter" sorting as "0001" born 1945
    And it is by "Ernest Nagel" sorting as "0002" born 1901
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      authors:
      - {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
      - {name: Ernest Nagel, sortKey: '0002', born: 1901}
      """
