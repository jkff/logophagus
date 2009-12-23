package org.lf.parser.CSVParser;

import java.io.IOException;

/**
* Created by IntelliJ IDEA.
* User: jkff
* Date: Dec 18, 2009
* Time: 5:30:20 PM
* To change this template use File | Settings | File Templates.
*/
interface CharStream {
    int next() throws IOException;
}
