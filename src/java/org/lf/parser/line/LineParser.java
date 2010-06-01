package org.lf.parser.line;

import org.lf.io.ScrollableInputStream;
import org.lf.logs.Format;
import org.lf.logs.LineRecord;
import org.lf.logs.Record;
import org.lf.parser.Parser;

import java.io.IOException;

public class LineParser implements Parser {
    private transient ScrollableInputStream cachedStream;
    private transient Long cachedOffset;
    private transient byte[] cachedBytes;

    private final Format[] formats = new Format[]{Format.UNKNOWN_FORMAT};

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        byte[] b = readForwardUntilBorder(is);
        int length = b[b.length - 1] == '\n' ? b.length - 1 : b.length;
        return new LineRecord(bytesToStringASCII(b, length));
    }

    private static String bytesToStringASCII(byte[] b, int length) {
        char[] cs = new char[length];
        for(int i = 0; i < cs.length; ++i)
            cs[i] = (char)b[i];
        return new String(cs);
    }

    @Override
    public int findNextRecord(ScrollableInputStream is) throws IOException {
        return readForwardUntilBorder(is).length;
    }

    @Override
    public int findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0) return 0;
        byte stopByte = (byte) '\n';
        byte[] res = is.readBackwardUntil(stopByte);
        return res[0] == stopByte ? res.length : res.length + 1;
    }

    @Override
    public Format[] getFormats() {
        return formats;
    }

    private synchronized byte[] readForwardUntilBorder(ScrollableInputStream is) throws IOException {
        if (!is.isSameSource(cachedStream) || is.getOffset() != cachedOffset) {
            cachedStream = is;
            cachedOffset = is.getOffset();
            cachedBytes = is.readForwardUntil((byte) '\n');
        }
        return cachedBytes;
    }
}
