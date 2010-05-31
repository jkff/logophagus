package org.lf.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on: 26.03.2010 22:02:23
 */
public class IOUtils {
    public static int pump(InputStream src, int n, OutputStream dest) throws IOException {
        byte[] buf = new byte[1 << 20];
        int total = 0;
        int cb;
        while (n > 0 && -1 != (cb = src.read(buf, 0, Math.min(n, buf.length)))) {
            n -= cb;
            total += cb;
            dest.write(buf, 0, cb);
        }
        return total;
    }

    public static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available());
        while (pump(is, Integer.MAX_VALUE, bos) > 0)
            ;
        return bos.toByteArray();
    }
}
