package org.lf.io.compressed;

import org.lf.io.BufferPool;
import org.lf.io.BufferScrollableInputStream;
import org.lf.io.RandomAccessFileIO;
import org.lf.parser.ScrollableInputStream;
import org.lf.util.CountingInputStream;
import org.lf.util.Function;
import org.lf.util.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CompressedRandomAccessIO implements RandomAccessFileIO {
    private String fileName;
    private int chunkSize;
    private int numBuffers;
    private Codec codec;

    private long compressedSize;
    private long decompressedSize;
    private Map<Long, File> base2chunkFile = new HashMap<Long, File>();
    private BufferPool<byte[], Long, Long> bufferPool;

    public CompressedRandomAccessIO(String fileName, int chunkSize, int numBuffers, Codec codec) {
        this.fileName = fileName;
        this.chunkSize = chunkSize;
        this.numBuffers = numBuffers;
        this.codec = codec;
    }

    public void init(ProgressListener progressListener) throws IOException {
        CountingInputStream cfis = new CountingInputStream(new FileInputStream(fileName));
        InputStream is = codec.decompress(cfis) ;
        try {
            decompressedSize = 0;
            compressedSize = new File(fileName).length();
            for(long base = 0; ; base += chunkSize) {
                if(!progressListener.reportProgress(1.0 * cfis.getBytesRead() / compressedSize))
                    break;
                // Instead of base, we should be using the number of compressed bytes read
                File chunkFile = File.createTempFile(new File(fileName).getName()+".chunk", "compressed");
                chunkFile.deleteOnExit();
                OutputStream os = codec.compress(new FileOutputStream(chunkFile));
                try {
                    long cb = IOUtils.pump(is, chunkSize, os);
                    if (cb <= 0)
                        break;
                    decompressedSize += cb;
                } finally {
                    os.close();
                }                                     
                base2chunkFile.put(base, chunkFile);
            }
        } finally {
            is.close();
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
                    File chunkFile = base2chunkFile.get(base);
                    return IOUtils.readInputStream(codec.decompress(new FileInputStream(chunkFile)));
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
        return new BufferScrollableInputStream(bufferPool, decompressedSize, offset);
    }

    @Override
    public long length() throws IOException {
        return decompressedSize;
    }
}
