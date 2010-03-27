package org.lf.io;

import org.lf.io.BufferPool;
import org.lf.io.BufferScrollableInputStream;
import org.lf.io.RandomAccessFileIO;
import org.lf.parser.ScrollableInputStream;
import org.lf.util.CountingInputStream;
import org.lf.util.Function;
import org.lf.util.ProgressListener;
import org.lf.zlib.RandomAccessGzip;

import java.io.*;
import java.util.Arrays;

public class GzipRandomAccessIO implements RandomAccessFileIO {
    private String fileName;
    private int chunkSize;
    private int numBuffers;

    private RandomAccessGzip.Index idx;
    private BufferPool<byte[], Long, Long> bufferPool;

    public GzipRandomAccessIO(String fileName, int chunkSize, int numBuffers) {
        this.fileName = fileName;
        this.chunkSize = chunkSize;
        this.numBuffers = numBuffers;
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

        Function<Long,Long> TRUNCATE_BUF = new Function<Long, Long>() {
            @Override
            public Long apply(Long offset) {
                return (offset / chunkSize) * chunkSize;  
            }
        };

        Function<Long,byte[]> LOAD_BUF = new Function<Long, byte[]>() {
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

        Function DO_NOTHING = new Function() { public Object apply(Object buffer) { return null; } };
        this.bufferPool = new BufferPool<byte[], Long, Long>(numBuffers, TRUNCATE_BUF, LOAD_BUF, DO_NOTHING);
    }

    @Override
    public String getFileName() {
        return fileName;
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
