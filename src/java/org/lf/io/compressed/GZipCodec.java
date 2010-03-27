package org.lf.io.compressed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCodec implements Codec {
    @Override
    public OutputStream compress(OutputStream out) throws IOException {
        return new GZIPOutputStream(out); 
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new GZIPInputStream(in);
    }
}
