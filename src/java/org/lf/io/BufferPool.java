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
public class BufferPool<B, K, H> {
    private int maxBuffers;
    private Function<K, H> hashKey;
    private Function<H, B> makeBuffer;
    private Function<Buffer, Void> releaseBuffer;

    private Map<H, Buffer> hash2buf = new HashMap<H, Buffer>();

    public class Buffer {
        private int refCount;
        public final H hash;
        public final B data;

        private Buffer(int refCount, H hash, B data) {
            this.refCount = refCount;
            this.hash = hash;
            this.data = data;
        }
    }

    public BufferPool(int maxBuffers, Function<K, H> hashKey, Function<H, B> makeBuffer, Function<Buffer, Void> releaseBuffer) {
        this.maxBuffers = maxBuffers;
        this.hashKey = hashKey;
        this.makeBuffer = makeBuffer;
        this.releaseBuffer = releaseBuffer;
    }

    // Invariant : Every result of getBuffer() must be
    // consequently released by a call of releaseBuffer().

    // Returns a buffer such that 'position' is inside its extent.
    // Blocks until a buffer is available.

    public synchronized Buffer getBuffer(K key) throws InterruptedException {
        // ...Get an existing buffer if possible and increase its refCount
        H hash = hashKey.apply(key);

        if (hash2buf.containsKey(hash)) {
            Buffer b = hash2buf.get(hash);
            b.refCount++;
            return b;
        }

        // Wait till there's space in the buffer pool
        if (hash2buf.size() >= maxBuffers) {
            // No space: garbageCollect() and try again.
            while (true) {
                garbageCollectBuffers();
                if (hash2buf.size() >= maxBuffers)
                    wait();
                else
                    return getBuffer(key);
            }
        }

        B data = this.makeBuffer.apply(hash);

        Buffer b = new Buffer(1, hash, data);
        hash2buf.put(hash, b);

        notifyAll();
        return b;
    }

    private synchronized void garbageCollectBuffers() {
        for (Iterator<H> it = hash2buf.keySet().iterator(); it.hasNext();) {
            H hash = it.next();
            Buffer b = hash2buf.get(hash);
            if (b.refCount == 0) {
                it.remove();
            }
        }
    }

    synchronized void releaseBuffer(Buffer buf) {
        buf.refCount--;
        if (buf.refCount == 0) {
            this.releaseBuffer.apply(buf);
            notifyAll();
        }
    }

    // Tells that buffer 'buf' is no longer needed,
    // buf a buffer with key 'newKey' is.
    // Does nothing if 'newKey' maps into 'buf',
    // otherwise frees 'buf' and allocates a new buffer.
    public Buffer move(Buffer buf, K newKey) throws IOException, InterruptedException {
        if(hashKey.apply(newKey).equals(buf.hash)) {
            return buf;
        } else {
            releaseBuffer(buf);
            return getBuffer(newKey);
        }
    }
}
