package org.lf.parser.line;

import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordImpl;
import org.lf.parser.*;

import java.io.IOException;

class LineParser implements Parser {
    private final Format[] formats = new Format[]{Format.UNKNOWN_FORMAT};

    private final Sink emptySink = new Sink() {
        @Override
        public void onChar(char c) {
        }
    };

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        ReadableSink sink = new ReadableSink() {
            @Override
            public String getReceivedChars() {
                return getContents().toString();
            }
        };

        getLineFromCharStream(forward(is), sink);
        return new RecordImpl(new String[]{sink.getReceivedChars()}, Format.UNKNOWN_FORMAT);
    }

    @Override
    public long findNextRecord(ScrollableInputStream is) throws IOException {
        return getLineFromCharStream(forward(is), emptySink);
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0)
            return 0;
        return getLineFromCharStream(backward(is), emptySink);
    }

    @Override
    public Format[] getFormats() {
        return formats;
    }

    private long getLineFromCharStream(CharStream cs, Sink sink) throws IOException {
        long offset = 0;
        int c;
        while (true) {
            ++offset;
            c = cs.next();
            if (c == -1 || c == '\n')
                return offset;
            sink.onChar((char) c);
        }
    }

    private CharStream forward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                return is.read();
            }
        };
    }

    private CharStream backward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                return is.readBack();
            }
        };
    }
}
