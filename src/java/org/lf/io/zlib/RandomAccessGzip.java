package org.lf.io.zlib;

import com.sun.jna.Memory;
import org.lf.util.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

public class RandomAccessGzip {
    public interface Index {
        int read(RandomAccessFile f, long origin, byte[] buf, int offset, int len) throws IOException;

        long decompressedSize();
    }

    private static class IndexImpl implements Index {
        private static final int BUF_SIZE = 1048576;

        private final List<ZRan.Point> idx;
        private final long decompressedSize;
        private final Memory mem;
        private final ByteBuffer memBuf;

        private IndexImpl(List<ZRan.Point> idx, long decompressedSize) {
            this.idx = idx;
            this.decompressedSize = decompressedSize;
            this.mem = new Memory(BUF_SIZE);
            this.memBuf = mem.getByteBuffer(0, BUF_SIZE);
        }

        @Override
        public long decompressedSize() {
            return decompressedSize;
        }

        @Override
        public synchronized int read(RandomAccessFile f, long origin, byte[] buf, int offset, int len) throws IOException {
            int total = 0;
            while (len > 0) {
                int n = ZRan.extract(f, idx, origin, mem, Math.min(len, BUF_SIZE));
                if (n == 0)
                    break;
                total += n;
                memBuf.position(0);
                memBuf.get(buf, offset, n);
                offset += n;
                len -= n;
            }
            return total;
        }
    }

    public static Index index(InputStream input, long span, ProgressListener<Long> listener) throws IOException {
        long[] holder = {0L};
        List<ZRan.Point> idx = ZRan.build_index(input, span, holder, listener);
        return idx == null ? null : new IndexImpl(idx, holder[0]);
    }

    public static IndexMemento indexToMemento(Index index) {
        IndexImpl idx = (IndexImpl) index;
        return new IndexMemento(idx.idx, idx.decompressedSize);
    }

    public static Index indexFromMemento(IndexMemento memento) {
        IndexMemento p = (IndexMemento) memento;
        return new IndexImpl(p.idx, p.decompressedSize);
    }
}
