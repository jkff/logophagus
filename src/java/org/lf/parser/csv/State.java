package org.lf.parser.csv;

/**
* Created by IntelliJ IDEA.
* User: jkff
* Date: Dec 18, 2009
* Time: 5:31:01 PM
* To change this template use File | Settings | File Templates.
*/
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
