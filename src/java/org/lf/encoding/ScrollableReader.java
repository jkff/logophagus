package org.lf.encoding;

import java.io.IOException;

public interface ScrollableReader {
    int next() throws IOException;
    int prev() throws IOException;

    void scrollToBegin() throws IOException;
    void scrollToEnd() throws IOException;

    /**
     * @param c - char on which reading must be stopped so that invocation of prev() must return int representation of c
     * @return true if c found, false otherwise.
     * @throws IOException
     */
    boolean scrollForwardUntil(char c) throws IOException;

    /**
     * @param c - char on which reading must be stopped so that invocation of next() must return int representation of c
     * @return true if c found, false otherwise.
     * @throws IOException
     */
    boolean scrollBackwardUntil(char c) throws IOException;

    /**
     * @param c - char on which reading must be stopped
     * @return char sequence - that contain chars between current offset and offset of scrollToBegin char = c after current offset.
     * current offset char and char c are also in result char sequence
     * @throws IOException
     */
    CharSequence readForwardUntil(char c) throws IOException;

    /**
     * @param c - char on which reading must be stopped
     * @return char sequence -  that contain chars between current offset and offset of scrollToBegin char = c before current offset.
     * current offset char is not in string and char c in result char sequence
     * @throws IOException
     */
    CharSequence readBackwardUntil(char c) throws IOException;

    boolean isSameSource(ScrollableReader scrollableReader);

    void scrollToOffset(long offset) throws IOException;

    long getCurrentOffset();
}
