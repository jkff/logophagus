package org.lf.parser;

import java.io.IOException;
import java.io.InputStream;

public abstract class ScrollableInputStream extends InputStream {
    public abstract int readBack() throws IOException;

    /**
     * @param offset - amount of bytes to move backward
     * @return amount of bytes actually moved
     * @throws IOException
     */
    public abstract long scrollBack(long offset) throws IOException;

    /**
     * @param offset - amount of bytes to move forward
     * @return amount of bytes actually moved
     * @throws IOException
     */
    public abstract long scrollForward(long offset) throws IOException;

    /**
     * @param newOffset - amount of bytes from begin of that stream
     * @throws IOException
     */
    public abstract void scrollTo(long newOffset) throws IOException;

    /**
     * @param b - byte on which reading must be stopped
     * @return byte[] that contain bytes between current offset and offset of first byte = b after current offset.
     *         current offset byte and byte b are also included in array
     * @throws IOException
     */
    public abstract byte[] readForwardUntil(byte b) throws IOException;

    /**
     * @param b - byte on which reading must be stopped
     * @return byte[] that contain bytes between current offset and offset of first byte = b before current offset.
     *         current offset byte is not included and byte b is included in array
     * @throws IOException
     */
    public abstract byte[] readBackwardUntil(byte b) throws IOException;

    public abstract long getOffset();

    public abstract boolean isSameSource(ScrollableInputStream other);
}
