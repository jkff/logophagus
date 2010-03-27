package org.lf.io.compressed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCodec implements Codec {
    @Override
    public OutputStream compress(OutputStream out) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(out);
        zos.putNextEntry(new ZipEntry("data"));
        return zos;
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        ZipInputStream zis = new ZipInputStream(in);
        zis.getNextEntry();
        return zis;
    }
}
