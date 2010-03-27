package org.lf.io.compressed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Codec {
    public OutputStream compress(OutputStream out) throws IOException;
    public InputStream decompress(InputStream in) throws IOException;
}
