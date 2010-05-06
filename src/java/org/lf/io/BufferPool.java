package org.lf.io;

import org.lf.util.Function;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides buffers with data of type 'B' by fine-keys of type 'K'
 * where two keys may map into the same hash (and thus the same buffer)
 * of type 'H'.
 */
public class BufferPool<K, H> {
    public static final int MAX_BUFFERS = 50;

    private Function<K, H> hashKey;
    private Function<H, byte[]> makeBuffer;
    private Function<Buffer, Void> releaseBuffer;
    private final Object owner;

    private static final Map<HashWithOwner, Object> hash2buf = new HashMap<HashWithOwner, Object>();

    private static class HashWithOwner {
        public final Object hashOwner;
        public final Object hash;

        public HashWithOwner(Object hashOwner, Object hash) {
            this.hashOwner = hashOwner;
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HashWithOwner)) return false;

            HashWithOwner that = (HashWithOwner) o;

            if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
            if (hashOwner != null ? !hashOwner.equals(that.hashOwner) : that.hashOwner != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = hashOwner != null ? hashOwner.hashCode() : 0;
            result = 31 * result + (hash != null ? hash.hashCode() : 0);
            return result;
        }
    }

    public class Buffer {
        private int refCount;
        public final H hash;
        public final byte[] data;

        private Buffer(int refCount, H hash, byte[] data) {
            this.refCount = refCount;
            this.hash = hash;
            this.data = data;
        }
    }

    public BufferPool(Object owner, Function<K, H> hashKey, Function<H, byte[]> makeBuffer, Function<Buffer, Void> releaseBuffer) {
        this.hashKey = hashKey;
        this.makeBuffer = makeBuffer;
        this.releaseBuffer = releaseBuffer;
        this.owner = owner;
    }

    // Invariant : Every result of getBuffer() must be
    // consequently released by a call of releaseBuffer().

    // Returns a buffer such that 'position' is inside its extent.
    // Blocks until a buffer is available.

    public Buffer getBuffer(K key) throws InterruptedException {
        // ...Get an existing buffer if possible and increase its refCount
        synchronized (hash2buf) {
            H hash = hashKey.apply(key);

            HashWithOwner mapKey = new HashWithOwner(owner, hash);

            if (hash2buf.containsKey(mapKey)) {
                Buffer b = (Buffer) hash2buf.get(mapKey);
                b.refCount++;
                return b;
            }

            // Wait till there's space in the buffer pool
            if (hash2buf.size() >= MAX_BUFFERS) {
                // No space: garbageCollect() and try again.
                while (true) {
                    garbageCollectBuffers();
                    if (hash2buf.size() >= MAX_BUFFERS)
                        hash2buf.wait();
                    else
                        return getBuffer(key);
                }
            }

            byte[] data = this.makeBuffer.apply(hash);

            Buffer b = new Buffer(1, hash, data);
            hash2buf.put(mapKey, b);

            hash2buf.notifyAll();
            return b;
        }
    }

    private void garbageCollectBuffers() {
        synchronized (hash2buf) {
            for (Iterator<HashWithOwner> it = hash2buf.keySet().iterator(); it.hasNext();) {
                HashWithOwner key = it.next();
                Buffer b = (Buffer) hash2buf.get(key);
                if (b.refCount == 0) {
                    it.remove();
                }
            }
        }
    }

    void releaseBuffer(Buffer buf) {
        buf.refCount--;
        if (buf.refCount == 0) {
            this.releaseBuffer.apply(buf);
            synchronized (hash2buf) {
                // No need to call notify() if surely noone is calling wait().
                // It is so if there are enough buffers.
                if(hash2buf.size() >= MAX_BUFFERS) {
                    // After 1 buffer has been released, only 1 buffer can be acquired,
                    // t.i. only 1 thread should be waked up
                    hash2buf.notify();
                }
            }
        }
    }

    // Tells that buffer 'buf' is no longer needed,
    // buf a buffer with key 'newKey' is.
    // Does nothing if 'newKey' maps into 'buf',
    // otherwise frees 'buf' and allocates a new buffer.

    public Buffer move(Buffer buf, K newKey) throws IOException, InterruptedException {
        synchronized (hash2buf) {
            if (hashKey.apply(newKey).equals(buf.hash)) {
                return buf;
            } else {
                releaseBuffer(buf);
                return getBuffer(newKey);
            }
        }
    }
}
