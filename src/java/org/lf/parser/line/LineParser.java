package org.lf.parser.line;

import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordImpl;
import org.lf.parser.Parser;
import org.lf.parser.ScrollableInputStream;

import java.io.IOException;

public class LineParser implements Parser {
    private ScrollableInputStream cachedStream;
    private Long cachedOffset;
    private byte[] cachedBytes;

    private final Format[] formats = new Format[]{Format.UNKNOWN_FORMAT};

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        byte b[] = readForwardUntilBorder(is);
        int length = b[b.length - 1] == '\n' ? b.length - 1 : b.length;
        return new RecordImpl(new String[]{new String(b, 0, length, "us-ascii")}, Format.UNKNOWN_FORMAT);

    }

    @Override
    public long findNextRecord(ScrollableInputStream is) throws IOException {
        return readForwardUntilBorder(is).length;
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0) return 0;
        return is.readBackwardUntil((byte) '\n').length;
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
