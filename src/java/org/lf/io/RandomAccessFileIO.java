package org.lf.io;

import org.lf.parser.ScrollableInputStream;

import java.io.IOException;

public interface RandomAccessFileIO {
    String getFileName();

    ScrollableInputStream getInputStreamFrom(long offset) throws IOException;

    long length() throws IOException;
}
