package org.lf.parser.regex;

import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordImpl;
import org.lf.parser.CharStream;
import org.lf.parser.Parser;
import org.lf.parser.ScrollableInputStream;
import org.lf.parser.Sink;
import org.lf.util.Triple;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpParser implements Parser {
    private final Format[] regexpFormats;
    private final Pattern[] patterns;
    private final char recordDelimiter;
    private final int maxLinesPerRecord;

    private abstract class ReadableSink extends Sink {
        public abstract String getReceivedChars();
    }

    private class OrderedReadableSink extends ReadableSink {
        private final boolean isForward;

        public OrderedReadableSink(boolean isForward) {
            this.isForward = isForward;
        }

        @Override
        public String getReceivedChars() {
            if (isForward)
                return getContents().toString();
            StringBuilder rev = new StringBuilder();
            rev.append(getContents());
            rev.reverse();
            return rev.toString();
        }
    }

    public RegexpParser(String[] regexps, Format[] regexpFormats, char recordDelimiter, int maxLinesPerRecord) {
        this.patterns = new Pattern[regexps.length];
        for (int i = 0; i < regexps.length; ++i) {
            patterns[i] = Pattern.compile(regexps[i]);
        }

        this.recordDelimiter = recordDelimiter;
        this.maxLinesPerRecord = maxLinesPerRecord;
        this.regexpFormats = regexpFormats;
    }

    private ScrollableInputStream cachedStream;
    private long cachedOffset;
    // Long - offset, Integer - index of pattern that matched (-1 means no pattern matched),
    // Object is a Matcher if a pattern matched, or a String (raw input line) if none matched 
    private Triple<Long, Integer, Object> cachedOffsetIndexMatch;

    @Override
    public synchronized long findNextRecord(ScrollableInputStream is) throws IOException {
        return findOIMforward(is).first;
    }

    private synchronized Triple<Long, Integer, Object> findOIMforward(ScrollableInputStream is) throws IOException {
        if (!is.isSameSource(cachedStream) || is.getOffset() != cachedOffset) {
            cachedStream = is;
            cachedOffset = is.getOffset();
            cachedOffsetIndexMatch = getRecordFromCharStream(forward(is), new OrderedReadableSink(true));
        }
        return cachedOffsetIndexMatch;
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0)
            return 0;
        return getRecordFromCharStream(backward(is), new OrderedReadableSink(false)).first;
    }

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        Triple<Long, Integer, Object> offsetIndexMatch = findOIMforward(is);

        if (offsetIndexMatch.second == -1) {
            String wholeString = (String) offsetIndexMatch.third;
            return new RecordImpl(new String[]{wholeString}, Format.UNKNOWN_FORMAT);
        }

        Field[] fields = regexpFormats[offsetIndexMatch.second].getFields();
        String[] cells = new String[regexpFormats[offsetIndexMatch.second].getFields().length];
        for (int i = 0; i < fields.length; ++i) {
            cells[i] = ((Matcher) offsetIndexMatch.third).group(i + 1);
        }
        return new RecordImpl(cells, regexpFormats[offsetIndexMatch.second]);
    }

    private Triple<Long, Integer, Object> getRecordFromCharStream(CharStream cs, ReadableSink sink) throws IOException {
        long firstLineBreakOffset = 0;
        long offset = 0;
        for (int i = 0; i < maxLinesPerRecord; ++i) {
            long temp = getLineFromCharStream(cs, sink);
            if (temp == 0)
                break;
            offset += temp;
            if (i == 0)
                firstLineBreakOffset = offset;

            String str = sink.getReceivedChars();
            for (int j = 0; j < patterns.length; ++j) {
                Matcher m = patterns[j].matcher(str);
                if (m.matches())
                    return new Triple<Long, Integer, Object>(offset, j, m);
            }
        }
        return new Triple<Long, Integer, Object>(firstLineBreakOffset, -1, sink.getReceivedChars());
    }

    private long getLineFromCharStream(CharStream cs, Sink sink) throws IOException {
        long offset = 0;
        int c;
        while (true) {
            ++offset;
            c = cs.next();
            if (c == -1 || c == recordDelimiter)
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

    @Override
    public Format[] getFormats() {
        return regexpFormats;
    }


}
