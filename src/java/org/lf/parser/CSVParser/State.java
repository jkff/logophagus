package org.lf.parser.CSVParser;

/**
* Created by IntelliJ IDEA.
* User: jkff
* Date: Dec 18, 2009
* Time: 5:31:01 PM
* To change this template use File | Settings | File Templates.
*/
enum State {
    BETWEEN_FIELDS,
    FIELD,
    RECORD_BORDER,
    IN_QUOTE,
    QUOTE_BEGIN,
    QUOTE_END,
    IN_QUOTE_ESCAPE,
    DOUBLE_QUOTE,
    ERROR;
}
