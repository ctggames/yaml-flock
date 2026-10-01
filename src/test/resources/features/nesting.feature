Feature: Mixing flow and block in one document

  This is what the library is for, and it is constrained by the format rather than by the code.

  YAML permits flow content inside block content, but not block content inside flow. Once a
  mapping is written inline, everything within it is committed to being inline, because there is
  no way to indent out of a flow collection and back again - the document would not parse.

  So a class whose scalars belong on one line, beside a map that belongs on several, cannot say so
  about itself. It nests the scalars in a flow-style record instead, and stays block itself. That
  is not a workaround for a missing annotation; it is the only arrangement the format allows.

  Scenario: scalars on one line beside a block map, by nesting a flow record
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it has genres "PHILOSOPHY, MATHEMATICS"
    And it is by "Douglas Hofstadter" sorting as "0001" born 1945
    And it has a "first" edition, HARDBACK, 777 pages, published 1979
    And it has a "reprint" edition, PAPERBACK, 832 pages, published 1999
    When the book is written as YAML
    Then the YAML is:
      """
      details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
      genres: [PHILOSOPHY, MATHEMATICS]
      authors:
      - {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
      editions:
        first: {format: HARDBACK, pages: 777, published: 1979}
        reprint: {format: PAPERBACK, pages: 832, published: 1999}
      """

  Scenario: the whole catalogue, every style in one document
    Given a book "0306406152" titled "Godel Escher Bach" published in 1979
    And it has genres "PHILOSOPHY, MATHEMATICS"
    And it is by "Douglas Hofstadter" sorting as "0001" born 1945
    And it has a "first" edition, HARDBACK, 777 pages, published 1979
    And a catalogue "Ctgnz Reference Library" established 1987
    And the catalogue holds that book
    When the catalogue is written as YAML
    Then the YAML is:
      """
      name: Ctgnz Reference Library
      established: 1987
      books:
      - details: {isbn: "0306406152", title: "Godel Escher Bach", year: 1979}
        genres: [PHILOSOPHY, MATHEMATICS]
        authors:
        - {name: Douglas Hofstadter, sortKey: '0001', born: 1945}
        editions:
          first: {format: HARDBACK, pages: 777, published: 1979}
      """
