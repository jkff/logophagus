package org.lf.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created on: 27.03.2010 10:47:17
 */
public class CountingInputStream extends InputStream {
    private final InputStream delegate;
    private long bytesRead = 0;

    public CountingInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        int res = delegate.read();
        if (res != -1) ++bytesRead;
        return res;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = delegate.read(b);
        if (n != -1) bytesRead += n;
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = delegate.read(b, off, len);
        if (n != -1) bytesRead += n;
        return n;
    }

    @Override
    public long skip(long s) throws IOException {
        long n = delegate.skip(s);
        if (n != -1) bytesRead += n;
        return n;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
