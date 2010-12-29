package org.lf.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created on: 26.03.2010 21:49:58
 */
public class BufferScrollableInputStream implements ScrollableInputStream {
    private BufferPool<Long, Long>.Buffer buf;

    private int offsetInBuffer;
    private boolean isOpen;
    private BufferPool<Long, Long> bufferPool;
    private long fileSize;

    public BufferScrollableInputStream(BufferPool<Long, Long> bufferPool, long fileSize, long offset) throws IOException {
        try {
            this.buf = bufferPool.getBuffer(offset);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.getBuffer()");
        }
        this.offsetInBuffer = (int) (offset - this.buf.base);

        this.isOpen = true;
        this.bufferPool = bufferPool;
        this.fileSize = fileSize;
    }

    //absolute scroll

    @Override
    public void scrollTo(long newOffset) throws IOException {
        try {
            this.buf = bufferPool.move(this.buf, newOffset == fileSize ? fileSize - 1: newOffset);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        this.offsetInBuffer = (int) (newOffset - this.buf.base);
    }

    @Override
    public byte[] readForwardUntil(byte b) throws IOException {
        ensureOpen();
        long initialPos = this.offsetInBuffer + this.buf.base;
        boolean shiftedBuffer = false;
        top: while(true) {
            byte[] data = buf.data;
            for(int i = this.offsetInBuffer; i < data.length; ++i) {
                if(data[i] == b) {
                    if(!shiftedBuffer) {
                        // Simplest case
                        byte[] res = Arrays.copyOfRange(data, this.offsetInBuffer, i + 1);
                        this.offsetInBuffer = i + 1;
                        return res;
                    }
                    this.offsetInBuffer = i + 1;
                    break top;
                }
            }
            shiftedBuffer = true;
            if(!shiftNextBuffer())
                break;
        }

        long delta = this.offsetInBuffer + this.buf.base - initialPos;
        scrollTo(initialPos);
        byte[] result = new byte[(int) delta];
        int n = read(result);
        if(n != result.length) {
            return Arrays.copyOf(result, n);
        } else {
            return result;
        }
    }

    @Override
    public byte[] readBackwardUntil(byte b) throws IOException {
        ensureOpen();
        long curFilePos = this.offsetInBuffer + this.buf.base;
        byte cur;
        do {
            if (this.offsetInBuffer == 0) {
                if (!shiftPrevBuffer())
                    break;
                this.offsetInBuffer = this.buf.data.length;
            }
            --this.offsetInBuffer;
            cur = this.buf.data[this.offsetInBuffer];
        } while (cur != b);

        long delta = curFilePos - this.offsetInBuffer - this.buf.base;

        byte[] result = new byte[(int) delta];
        int n = read(result);
        scrollBack(delta);
        if(n != result.length) {
            return Arrays.copyOf(result, n);
        } else {
            return result;
        }
    }

    @Override
    public long scrollBack(long offset) throws IOException {
        ensureOpen();
        long scrolled;
        long curFilePos = this.offsetInBuffer + this.buf.base;
        try {
            if (curFilePos <= offset) {
                scrolled = curFilePos;
                this.buf = bufferPool.move(this.buf, 0L);
                this.offsetInBuffer = 0;
            } else {
                this.buf = bufferPool.move(this.buf, curFilePos - offset);
                this.offsetInBuffer = (int) (curFilePos - offset - this.buf.base);
                scrolled = offset;
            }
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        return scrolled;

    }

    @Override
    public long scrollForward(long offset) throws IOException {
        ensureOpen();
        long maxOffset = fileSize;
        long curFilePos = this.offsetInBuffer + this.buf.base;
        long scrolled;
        try {
            if (curFilePos + offset >= maxOffset) {
                scrolled = maxOffset - curFilePos;
                this.buf = bufferPool.move(buf, maxOffset - 1);
                this.offsetInBuffer = this.buf.data.length;
            } else {
                this.buf = bufferPool.move(buf, curFilePos + offset);
                this.offsetInBuffer = (int) (curFilePos + offset - this.buf.base);
                scrolled = offset;
            }
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        return scrolled;
    }

    @Override
    public long getOffset() {
        return this.buf.base + this.offsetInBuffer;
    }

    @Override
    public long getMaxOffset() {
        return this.fileSize;
    }

    @Override
    public boolean isSameSource(ScrollableInputStream other) {
        return (other instanceof BufferScrollableInputStream) &&
                ((BufferScrollableInputStream) other).bufferPool == this.bufferPool;
    }

    @Override
    public int next() throws IOException {
        ensureOpen();
        if (this.offsetInBuffer == this.buf.data.length && !shiftNextBuffer()) {
            return -1;
        }
        return this.buf.data[this.offsetInBuffer++];
    }

    @Override
    public boolean hasNext() {
        return this.offsetInBuffer + this.buf.base != fileSize;
    }

    @Override
    public boolean hasPrev() {
        return this.offsetInBuffer + this.buf.base != 0L;
    }


    @Override
    public int prev() throws IOException {
        ensureOpen();
        if (this.offsetInBuffer == 0 && !shiftPrevBuffer()) {
            return -1;
        }
        return this.buf.data[--this.offsetInBuffer];
    }

    public int read(byte[] b) throws IOException {
        int needToRead = b.length;
        int bytesRead = 0;
        do {
            int availableInCurBuffer = this.buf.data.length - this.offsetInBuffer;
            int delta = Math.min(needToRead, availableInCurBuffer);
            System.arraycopy(this.buf.data, this.offsetInBuffer, b, bytesRead, delta);
            bytesRead += delta;
            this.offsetInBuffer += delta;
            needToRead -= delta;
            if (availableInCurBuffer < needToRead) {
                if (!shiftNextBuffer()) {
                    this.offsetInBuffer = this.buf.data.length - 1;
                    return 0;
                }
            }
        } while (needToRead > 0);

        return bytesRead;
    }

    private boolean shiftPrevBuffer() throws IOException {
        if (this.buf.base == 0)
            return false;
        try {
            this.buf = bufferPool.move(this.buf, this.buf.base - 1);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException from bufferPool.move()");
        }
        this.offsetInBuffer = this.buf.data.length;
        return true;
    }

    private boolean shiftNextBuffer() throws IOException {
        if (isAtEOF())
            return false;
        try {
            this.buf = bufferPool.move(this.buf, this.buf.base + this.buf.data.length);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException from bufferPool.move()");
        }
        this.offsetInBuffer = 0;
        return true;
    }

    private boolean isAtEOF() {
        return this.buf.base + this.offsetInBuffer == fileSize;
    }


    @Override
    public void close() throws IOException {
        ensureOpen();
        bufferPool.releaseBuffer(buf);
        this.isOpen = false;
    }

    private void ensureOpen() {
        if (!isOpen)
            throw new IllegalStateException("Stream closed");
    }
}
