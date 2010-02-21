package org.lf.parser.csv;

import java.io.IOException;

interface CharStream {
    int next() throws IOException;
}
