package org.lf.io;

import java.io.IOException;

/**
 * Created on: 26.03.2010 21:49:58
 */
public class BufferScrollableInputStream extends ScrollableInputStream {
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
        this.offsetInBuffer = (int) (offset - this.buf.hash);

        this.isOpen = true;
        this.bufferPool = bufferPool;
        this.fileSize = fileSize;
    }

    //absolute scroll

    @Override
    public void scrollTo(long newOffset) throws IOException {
        try {
            this.buf = bufferPool.move(this.buf, newOffset);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        this.offsetInBuffer = (int) (newOffset - this.buf.hash);
    }

    @Override
    public byte[] readForwardUntil(byte b) throws IOException {
        ensureOpen();
        long curFilePos = this.offsetInBuffer + this.buf.hash;
        byte cur;
        do {
            if (this.offsetInBuffer == this.buf.data.length - 1) {
                cur = this.buf.data[this.offsetInBuffer];
                if (!shiftNextBuffer()) break;
            } else
                cur = this.buf.data[this.offsetInBuffer++];
        } while (cur != b);

        long delta = this.offsetInBuffer + this.buf.hash - curFilePos;
        scrollTo(curFilePos);
        byte[] result = new byte[(int) delta];
        read(result);
        return result;
    }

    @Override
    public byte[] readBackwardUntil(byte b) throws IOException {
        ensureOpen();
        long curFilePos = this.offsetInBuffer + this.buf.hash;
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

        long delta = curFilePos - this.offsetInBuffer - this.buf.hash;

        byte[] result = new byte[(int) delta];
        read(result);
        scrollBack(delta);
        return result;
    }

    //relative scroll

    @Override
    public long scrollBack(long offset) throws IOException {
        ensureOpen();
        long scrolled;
        long curFilePos = this.offsetInBuffer + this.buf.hash;
        try {
            if (curFilePos <= offset) {
                scrolled = curFilePos;
                this.buf = bufferPool.move(this.buf, 0L);
                this.offsetInBuffer = 0;
            } else {
                this.buf = bufferPool.move(this.buf, curFilePos - offset);
                this.offsetInBuffer = (int) (curFilePos - offset - this.buf.hash);
                scrolled = offset;
            }
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        return scrolled;

    }

    //relative scroll

    @Override
    public long scrollForward(long offset) throws IOException {
        ensureOpen();
        long maxOffset = fileSize - 1;
        long curFilePos = this.offsetInBuffer + this.buf.hash;
        long scrolled;
        try {
            if (curFilePos + offset > maxOffset) {
                scrolled = maxOffset - curFilePos;
                this.buf = bufferPool.move(buf, maxOffset);
                this.offsetInBuffer = this.buf.data.length - 1;
            } else {
                this.buf = bufferPool.move(buf, curFilePos + offset);
                this.offsetInBuffer = (int) (curFilePos + offset - this.buf.hash);
                scrolled = offset;
            }
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException in bufferPool.move()");
        }
        return scrolled;
    }

    @Override
    public long getOffset() {
        return this.buf.hash + this.offsetInBuffer;
    }

    @Override
    public boolean isSameSource(ScrollableInputStream other) {
        return (other instanceof BufferScrollableInputStream) &&
                ((BufferScrollableInputStream) other).bufferPool == this.bufferPool;
    }

    //relative read

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (this.offsetInBuffer == this.buf.data.length - 1) {
            int temp = this.buf.data[this.offsetInBuffer];
            if (!shiftNextBuffer()) {
                return -1;
            }
            return temp;
        }
        return this.buf.data[this.offsetInBuffer++];
    }


    @Override
    public int readBack() throws IOException {
        ensureOpen();
        if (this.offsetInBuffer != 0)
            return this.buf.data[--this.offsetInBuffer];

        if (!shiftPrevBuffer())
            return -1;

        this.offsetInBuffer = this.buf.data.length - 1;
        return this.buf.data[this.offsetInBuffer];
    }


    //relative read

    @Override
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
        if (this.buf.hash == 0)
            return false;
        try {
            this.buf = bufferPool.move(this.buf, this.buf.hash - 1);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException from bufferPool.move()");
        }
        this.offsetInBuffer = 0;
        return true;
    }

    private boolean shiftNextBuffer() throws IOException {
        if (isAtEOF())
            return false;
        try {
            this.buf = bufferPool.move(this.buf, this.buf.hash + this.buf.data.length);
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException from bufferPool.move()");
        }
        this.offsetInBuffer = 0;
        return true;
    }

    private boolean isAtEOF() {
        return this.buf.hash + this.offsetInBuffer == fileSize - 1;
    }

    @Override
    public long skip(long n) {
        long skipped;
        try {
            skipped = scrollForward(n);
        } catch (Exception e) {
            skipped = 0;
        }
        return skipped;
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
        if (!isOpen)
            throw new IllegalStateException("Stream closed");
    }
}
