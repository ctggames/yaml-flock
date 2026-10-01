Feature: Choosing block style for one property

  A type can declare its own style, and most should: an Edition is three small facts that read
  better on one line, so Edition says so once and every catalogue benefits.

  But a default is not a verdict. The same Edition is a row in a table when it sits in a book's
  list of printings, and a description when an archivist reads it off a shelf mark. The shape
  belongs to whoever is doing the reading, which means it belongs to the owning property.

  The YamlBlockStyle annotation names the properties an owner wants in block style, overriding
  whatever their own type asked for.

  Note the asymmetry, which is YAML's and not this library's: block content may contain flow
  content, but flow content may not contain block. So an owner can force a property to block only
  where the owner itself is block. There is no annotation that would let a flow mapping hold a
  block one, because the document would not parse.

  Scenario: an edition is written inline, because its own type asks for that
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it has a "first" edition, HARDBACK, 777 pages, published 1979
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      editions:
        first: {format: HARDBACK, pages: 777, published: 1979}
      """

  Scenario: the very same type is written one field per line, because its owner asks for that
    Given an archive entry "QA9.H63" holding a HARDBACK edition of 777 pages published 1979
    When the archive entry is written as YAML
    Then the YAML is:
      """
      shelfMark: QA9.H63
      edition:
        format: HARDBACK
        pages: 777
        published: 1979
      """

  Scenario: the override reaches a map's entries, the map itself already being block
    Given a holdings record "QA9.H63" of a HARDBACK edition, "first", 777 pages, published 1979
    When the holdings record is written as YAML
    Then the YAML is:
      """
      shelfMark: QA9.H63
      editions:
        first:
          format: HARDBACK
          pages: 777
          published: 1979
      """
