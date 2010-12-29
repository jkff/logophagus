package org.lf.parser.csv;

enum State {
    ERROR,
    NON_QUOTED_FIELD,
    QUOTED_FIELD,
    QUOTE_END,
    QUOTE_BEGIN,
    DOUBLE_QUOTE,
    RECORD_BORDER,
    BETWEEN_FIELDS,
    SPACE_BETWEEN_FIELDS
}
