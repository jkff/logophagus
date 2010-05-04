package org.lf.parser.line;

import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordImpl;
import org.lf.parser.Parser;
import org.lf.parser.ScrollableInputStream;

import java.io.IOException;

class LineParser implements Parser {
    private final Format[] formats = new Format[]{Format.UNKNOWN_FORMAT};

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        byte b[] = is.readForwardUntil((byte) '\n');
        int length = b[b.length - 1] == '\n' ? b.length - 1 : b.length;
        return new RecordImpl(new String[]{new String(b, 0, length)}, Format.UNKNOWN_FORMAT);

    }

    @Override
    public long findNextRecord(ScrollableInputStream is) throws IOException {
        return is.readForwardUntil((byte) '\n').length;
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        byte b[] = is.readBackwardUntil((byte) '\n');
        if (b.length == 1) return findPrevRecord(is);
        return b.length;
    }

    @Override
    public Format[] getFormats() {
        return formats;
    }

}
