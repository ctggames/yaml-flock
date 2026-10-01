Feature: Choosing flow style for a type

  Block style is the default, and right for most things: a mapping per line, nested by indent,
  is what makes a large document navigable.

  Some types are not like that. An edition is three small facts; an author is a name and two
  details. Spread over four lines each they bury the document they are part of, and a catalogue
  holding a hundred of them becomes unreadable for no gain.

  The YamlFlowStyle annotation says so once, on the type, and every use of it benefits.

  Scenario: a type with no annotation is written in block style
    Given an index of "hofstadter" by title to "0306406152"
    When the index is written as YAML
    Then the YAML is:
      """
      byTitle:
        hofstadter:
        - '0306406152'
      """

  Scenario: an annotated type is written inline wherever it appears
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it is by "Douglas Hofstadter" sorting as "0001" born 1945
    And it has a "first" edition, HARDBACK, 777 pages, published 1979
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      authors:
      - {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
      editions:
        first: {format: HARDBACK, pages: 777, published: 1979}
      """
