package org.lf.io;

import org.lf.io.zlib.RandomAccessGzip;
import org.lf.util.CountingInputStream;
import org.lf.util.Function;
import org.lf.util.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class GzipRandomAccessIO implements RandomAccessFileIO {
    private String fileName;
    private int chunkSize;

    private RandomAccessGzip.Index idx;
    private BufferPool<Long, Long> bufferPool;

    public GzipRandomAccessIO(String fileName, int chunkSize) {
        this.fileName = fileName;
        this.chunkSize = chunkSize;
    }

    public void init(final ProgressListener<Double> progressListener) throws IOException {
        final long compressedSize = new File(fileName).length();
        CountingInputStream cfis = new CountingInputStream(new FileInputStream(fileName));
        try {
            idx = RandomAccessGzip.index(cfis, chunkSize, new ProgressListener<Long>() {
                public boolean reportProgress(Long progress) {
                    return progressListener.reportProgress(1.0 * progress / compressedSize);
                }
            });
        } finally {
            cfis.close();
        }
        progressListener.reportProgress(1.0);

        Function<Long, Long> TRUNCATE_BUF = new Function<Long, Long>() {
            @Override
            public Long apply(Long offset) {
                return (offset / chunkSize) * chunkSize;
            }
        };

        Function<Long, byte[]> LOAD_BUF = new Function<Long, byte[]>() {
            @Override
            public byte[] apply(Long base) {
                try {
                    byte[] buf = new byte[chunkSize];
                    RandomAccessFile f = new RandomAccessFile(fileName, "r");
                    try {
                        int n = idx.read(f, base, buf, 0, buf.length);
                        return n == chunkSize ? buf : Arrays.copyOf(buf, n);
                    } finally {
                        f.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Function DO_NOTHING = new Function() {
            public Object apply(Object buffer) {
                return null;
            }
        };
        this.bufferPool = new BufferPool<Long, Long>(this, TRUNCATE_BUF, LOAD_BUF, DO_NOTHING);
    }

    @Override
    public String getFileName() {
        return new File(fileName).getName();
    }

    @Override
    public ScrollableInputStream getInputStreamFrom(long offset) throws IOException {
        return new BufferScrollableInputStream(bufferPool, idx.decompressedSize(), offset);
    }

    @Override
    public long length() throws IOException {
        return idx.decompressedSize();
    }
}
