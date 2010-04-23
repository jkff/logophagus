package org.lf.io;

import org.lf.parser.ScrollableInputStream;
import org.lf.util.Function;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class MappedFile implements RandomAccessFileIO {
    private BufferPool<byte[], Long, Long> bufferPool;

    private File file;
    private final long fileSize;

    // File is divided into parts of equal size = bufSize (last part size may
    // differ from others, so buffers do not overlap
    private static final int MAX_BUFFERS_COUNT = 20;
    private static final long BUF_SIZE = 1000000;
    private static final Function<Long, Long> TRUNCATE_BUF = new Function<Long, Long>() {
        public Long apply(Long offset) {
            return (offset / BUF_SIZE) * BUF_SIZE;
        }
    };

    public MappedFile(final String fileName) throws FileNotFoundException {
        this.file = new File(fileName);

        fileSize = file.length();

        Function<Long, byte[]> LOAD_BUF = new Function<Long, byte[]>() {
            private RandomAccessFile raf = new RandomAccessFile(fileName, "r");

            public byte[] apply(Long base) {
                try {
                    long curBufSize = BUF_SIZE;
                    if (fileSize - base < BUF_SIZE) {
                        curBufSize = fileSize - base;
                    }

                    FileChannel rafChannel = raf.getChannel();
                    // surprisingly, rafChannel can be closed
                    while (!rafChannel.isOpen()) {
                        rafChannel.close();
                        raf.close();
                        raf = new RandomAccessFile(file.getAbsolutePath(), "r");
                        rafChannel = raf.getChannel();
                    }

                    MappedByteBuffer mbb = rafChannel.map(FileChannel.MapMode.READ_ONLY, base, curBufSize);
                    byte[] buf = new byte[(int) curBufSize];
                    mbb.get(buf);
                    rafChannel.close();
                    return buf;
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
        this.bufferPool = new BufferPool<byte[], Long, Long>(MAX_BUFFERS_COUNT, TRUNCATE_BUF, LOAD_BUF, DO_NOTHING);
    }

    public ScrollableInputStream getInputStreamFrom(final long offset) throws IOException {
        return new BufferScrollableInputStream(bufferPool, fileSize, offset);
    }

    public long length() {
        return this.fileSize;
    }

    public String getFileName() {
        return file.getName();
    }

}
