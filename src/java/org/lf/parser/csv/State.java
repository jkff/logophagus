package org.lf.parser.csv;

enum State {
    ERROR,
    FIELD,
    IN_QUOTE,
    QUOTE_END,
    QUOTE_BEGIN,
    DOUBLE_QUOTE,
    RECORD_BORDER,
    BETWEEN_FIELDS,
    IN_QUOTE_ESCAPE,
}
