package org.lf.parser;

import java.io.IOException;

public interface CharStream {
    int next() throws IOException;
}
