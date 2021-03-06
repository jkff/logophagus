package org.lf.io;

import java.io.File;
import java.io.IOException;

public interface RandomAccessFileIO {
    String getFileName();

    ScrollableInputStream getInputStreamFrom(long offset) throws IOException;

    long length();

    File getFile();
}
