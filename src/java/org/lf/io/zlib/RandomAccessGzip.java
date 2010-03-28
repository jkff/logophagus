package org.lf.io.zlib;

import com.sun.jna.Memory;
import org.lf.util.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

public class RandomAccessGzip {
    public interface Index {
        int read(RandomAccessFile f, long origin, byte[] buf, int offset, int len) throws IOException;

        long decompressedSize();
    }
    private static class IndexImpl implements Index {
        List<ZRan.Point> idx;
        long decompressedSize;

        private IndexImpl(List<ZRan.Point> idx, long decompressedSize) {
            this.idx = idx;
            this.decompressedSize = decompressedSize;
        }

        @Override
        public long decompressedSize() {
            return decompressedSize;
        }

        @Override
        public int read(RandomAccessFile f, long origin, byte[] buf, int offset, int len) throws IOException {
            Memory mem = new Memory(len);
            int n = ZRan.extract(f, idx, origin, mem, len);
            mem.getByteBuffer(0, len).get(buf, offset, len);
            return n; 
        }
    }

    public static Index index(InputStream input, long span, ProgressListener<Long> listener) throws IOException {
        long[] holder = {0L};
        List<ZRan.Point> idx = ZRan.build_index(input, span, holder, listener);
        return idx == null ? null : new IndexImpl(idx, holder[0]);
    }
}
