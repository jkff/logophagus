package org.lf.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Note that it has position before first byte and after last byte
 */
public interface ScrollableInputStream {
    /**
     * @return previous byte or -1 if there is no more bytes
     * @throws IOException if IOException occures
     */
    int prev() throws IOException;

    /**
     * @return next byte -1 if there is no more bytes
     * @throws IOException if there is no bytes left or IOException occures
     */
    int next() throws IOException;

    boolean hasPrev();
    boolean hasNext();

    /**
     * @param offset - amount of bytes from begin of that stream
     * @throws IOException
     */
    void scrollTo(long offset) throws IOException;

    /**
     * @param offset - amount of bytes to move backward
     * @return amount of bytes actually moved
     * @throws IOException
     */
    long scrollBack(long offset) throws IOException;

    /**
     * @param offset - amount of bytes to move forward
     * @return amount of bytes actually moved
     * @throws IOException
     */
    long scrollForward(long offset) throws IOException;

    /**
     * @param b - byte on which reading must be stopped
     * @return byte[] that contain bytes between current offset and offset of first byte = b after current offset.
     *         current offset byte and byte b are also included in array
     * @throws IOException
     */
    byte[] readForwardUntil(byte b) throws IOException;

    /**
     * @param b - byte on which reading must be stopped
     * @return byte[] that contain bytes between current offset and offset of first byte = b before current offset.
     *         current offset byte is not included and byte b is included in array
     * @throws IOException
     */
    byte[] readBackwardUntil(byte b) throws IOException;

    long getOffset();

    /**
     *
     * @return max offset that can be used in scrollTo() method.
     */
    long getMaxOffset();
    boolean isSameSource(ScrollableInputStream other);

    void close() throws IOException;
}
