package org.lf.io;

import java.io.IOException;

import org.lf.parser.ScrollableInputStream;

public interface RandomAccessFileIO {
    String getFileName();
    ScrollableInputStream getInputStreamFrom(long offset) throws IOException;
    long length() throws IOException;
}
