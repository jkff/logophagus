package org.lf.parser.regex;

import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordImpl;
import org.lf.parser.Parser;
import org.lf.parser.ScrollableInputStream;
import org.lf.util.Triple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpParser implements Parser {
    private final Format[] regexpFormats;
    private final Pattern[] patterns;
    private final char recordDelimiter;
    private final int maxLinesPerRecord;

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
    // Integer - relative offset, Integer - index of pattern that matched (-1 means no pattern matched),
    // Object is a Matcher if a pattern matched, or a String (raw input line) if none matched 
    private Triple<Integer, Integer, Object> cachedOffsetIndexMatch;

    @Override
    public synchronized long findNextRecord(ScrollableInputStream is) throws IOException {
        return findOIMforward(is).first;
    }

    private synchronized Triple<Integer, Integer, Object> findOIMforward(ScrollableInputStream is) throws IOException {
        if (!is.isSameSource(cachedStream) || is.getOffset() != cachedOffset) {
            cachedStream = is;
            cachedOffset = is.getOffset();
            cachedOffsetIndexMatch = getRecordFromCharStream(is, true);
        }
        return cachedOffsetIndexMatch;
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0) return 0;
        return getRecordFromCharStream(is, false).first;
    }

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        Triple<Integer, Integer, Object> offsetIndexMatch = findOIMforward(is);

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

    private Triple<Integer, Integer, Object> getRecordFromCharStream(ScrollableInputStream is, boolean isForward) throws IOException {
        int firstLineLength = 0;
        String firstLineString = null;
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        String curBytesString;

        for (int i = 0; i < maxLinesPerRecord; ++i) {
            byte b[] = isForward ? is.readForwardUntil((byte) recordDelimiter) : is.readBackwardUntil((byte) recordDelimiter);
            if (i == 0) {
                firstLineLength = b.length;
                byteArray.write(b);
            } else {
                if (isForward) byteArray.write(b);
                else byteArray.write(b, 0, b.length);
            }

            curBytesString = byteArray.toString("utf-8");
            if (i == 0) firstLineString = curBytesString;
            int length = curBytesString.length();

            for (int j = 0; j < patterns.length; ++j) {
                Matcher m = patterns[j].matcher(curBytesString.charAt(length - 1) == '\n' ?
                        curBytesString.substring(0, length - 1) : curBytesString);
                if (m.matches())
                    return new Triple<Integer, Integer, Object>(byteArray.size(), j, m);
            }

            if (b[isForward ? b.length - 1 : 0] != (byte) recordDelimiter) {
                if (!isForward) firstLineLength++;
                break;
            }
        }
        return new Triple<Integer, Integer, Object>(firstLineLength, -1, firstLineString);
    }

    @Override
    public Format[] getFormats() {
        return regexpFormats;
    }


}
