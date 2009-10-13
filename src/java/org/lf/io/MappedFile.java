package org.lf.io;

import org.lf.parser.ScrollableInputStream;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Map;

/**
 * User: jkff
 * Date: Oct 13, 2009
 * Time: 3:24:53 PM
 */
public class MappedFile implements RandomAccessFileIO {

    private String fileName;

    private class Buffer {
        int refCount;
        MappedByteBuffer buf;
    }

    private class BufferPool {
        private long bufSize;
        private int maxBuffers;

        private Map<Long, Buffer> base2buf;

        // Invariant : Every result of getBuffer() must be
        // consequently released by a call of releaseBuffer().

        // Returns a buffer such that 'position' is inside its extent.
        // Blocks until a buffer is available.
        Buffer getBuffer(long position) {
            // ...Get an existing buffer (and increase its refCount)
            // or create a new buffer with refCount = 1
        }

        void releaseBuffer(Buffer buf) {
            // ...decrement refCount, etc.
        }

        // Tells that buffer 'buf' is no longer needed,
        // buf a buffer that contains 'newOffset' is.
        // Does nothing if 'newOffset' lies in 'buf',
        // otherwise frees 'buf' and allocates a new buffer.
        public Buffer move(Buffer buf, long newOffset) {
            
        }
    }

    private BufferPool bufferPool;

    public MappedFile(String fileName) {
        this.fileName = fileName;
        this.bufferPool = new BufferPool();
    }

    public ScrollableInputStream getInputStreamFrom(final long ofs) {
        return new ScrollableInputStream() {
            private long offset;
            private Buffer buf;
            private boolean isOpen;

            {
                this.offset = ofs;
                this.buf = bufferPool.getBuffer(offset);
                this.isOpen = true;
            }

            @Override
            public long scrollBack(long offset) throws IOException {
                ensureOpen();
                this.offset -= offset;
                this.buf = bufferPool.move(buf, this.offset);
            }

            @Override
            public long scrollForward(long offset) throws IOException {
                ensureOpen();
                this.offset += offset;
                this.buf = bufferPool.move(buf, this.offset);
            }

            @Override
            public int read() throws IOException {
                byte[] res = new byte[1];
                if(read(res) == 0)
                    return -1;
                return (int)res[0];
            }

            @Override
            public int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                ensureOpen();

            }

            @Override
            public long skip(long n) throws IOException {
                return scrollForward(n);
            }

            @Override
            public int available() throws IOException {
                throw new UnsupportedOperationException("Who cares?");
            }

            @Override
            public void close() throws IOException {
                ensureOpen();
                bufferPool.releaseBuffer(buf);
                this.isOpen = false;
            }

            private void ensureOpen() {
                if(!isOpen)
                    throw new IllegalStateException("Stream closed");
            }
        };
    }

    public long length() {

    }
}
