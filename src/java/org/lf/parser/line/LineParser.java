package org.lf.parser.line;

import org.lf.encoding.ScrollableReader;
import org.lf.logs.Format;
import org.lf.logs.LineRecord;
import org.lf.logs.Record;
import org.lf.parser.Parser;

import java.io.IOException;

public class LineParser implements Parser {
    private final Format[] formats = new Format[]{Format.UNKNOWN_FORMAT};

    @Override
    public Record readRecord(ScrollableReader reader) throws IOException {
        CharSequence res = reader.readForwardUntil('\n');
        return new LineRecord(res);
    }

    @Override
    public long findNextRecord(ScrollableReader reader) throws IOException {
        long lastOffset = reader.getCurrentOffset();
        reader.scrollForwardUntil('\n');
        return lastOffset != reader.getCurrentOffset() ? reader.getCurrentOffset() : -1;
    }

    @Override
    public long findPrevRecord(ScrollableReader reader) throws IOException {
        long lastOffset = reader.getCurrentOffset();
        if (reader.prev() == -1) return -1;
        if (reader.scrollBackwardUntil('\n')) {
            reader.next();
        }
        return lastOffset != reader.getCurrentOffset() ? reader.getCurrentOffset() : -1;
    }

    @Override
    public Format[] getFormats() {
        return formats;
    }
}
