package org.lf.parser.regex;

import org.lf.io.ScrollableInputStream;
import org.lf.logs.*;
import org.lf.parser.Parser;
import org.lf.util.Triple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpParser implements Parser {
    private final Format[] regexpFormats;
    private final Pattern[] patterns;
    private final char recordDelimiter;
    private final int[] linesPerRecord;
    private int maxLinesPerRecord = 1;

    private transient ScrollableInputStream cachedStream;

    private transient long cachedOffset;

    // Integer - relative offset, Integer - index of pattern that matched (-1 means no pattern matched),
    // Object is a Matcher if a pattern matched, or a String (raw input line) if none matched
    private Triple<Integer, Integer, Object> cachedOffsetIndexMatch;

    public RegexpParser(String[] regexps, Format[] regexpFormats, char recordDelimiter, int[] linesPerRecord) {
        this.patterns = new Pattern[regexps.length];
        for (int i = 0; i < regexps.length; ++i) {
            patterns[i] = Pattern.compile(regexps[i]);
        }

        this.recordDelimiter = recordDelimiter;
        this.linesPerRecord = linesPerRecord;

        for (int cur : linesPerRecord) {
            if (cur > maxLinesPerRecord)
                maxLinesPerRecord = cur;
        }

        this.regexpFormats = regexpFormats;
    }

    @Override
    public synchronized int findNextRecord(ScrollableInputStream is) throws IOException {
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
    public int findPrevRecord(ScrollableInputStream is) throws IOException {
        // scroll back to \n symbol
        if (is.scrollBack(1) == 0) return 0;
        return getRecordFromCharStream(is, false).first + 1;
    }

    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        Triple<Integer, Integer, Object> offsetIndexMatch = findOIMforward(is);

        if (offsetIndexMatch.second == -1) {
            return new LineRecord((String) offsetIndexMatch.third);
        }

        return new RegexRecord((Matcher) offsetIndexMatch.third, regexpFormats[offsetIndexMatch.second]);
    }

    private Triple<Integer, Integer, Object> getRecordFromCharStream(ScrollableInputStream is, boolean isForward) throws IOException {
        int firstLineLength = 0;
        String firstLineString = null;
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        String curBytesString;

        for (int joinCount = 0; joinCount < maxLinesPerRecord; ++joinCount) {
            byte b[] = isForward ? is.readForwardUntil((byte) recordDelimiter) : is.readBackwardUntil((byte) recordDelimiter);

            if (isForward) byteArray.write(b);
            else byteArray.write(b, 0, b.length);

            curBytesString = byteArray.toString("us-ascii");
            int length = curBytesString.length();

            if (joinCount == 0) {
                firstLineLength = !isForward ?
                        curBytesString.charAt(0) == recordDelimiter ?
                                length - 1
                                :
                                length
                        :
                        length;
                firstLineString = curBytesString;
            }


            for (int i = 0; i < linesPerRecord.length; ++i) {
                if (linesPerRecord[i] != joinCount + 1) continue;

                Matcher m = patterns[i].matcher
                        (
                                curBytesString.length() != 0 && curBytesString.charAt(length - 1) == '\n'
                                        ?
                                        curBytesString.substring(0, length - 1)
                                        :
                                        curBytesString
                        );
                if (m.matches()) {
                    if (isForward)
                        return new Triple<Integer, Integer, Object>(byteArray.size(), i, m);
                    if (curBytesString.charAt(0) == recordDelimiter)
                        return new Triple<Integer, Integer, Object>(byteArray.size() - 1, i, m);
                    return new Triple<Integer, Integer, Object>(byteArray.size(), i, m);
                }

            }
            // if there is no recordDelimiter at the joined lines end then the begin/end of file occurs
            // and we can't continue to read next lines 
            if (curBytesString.charAt(isForward ? curBytesString.length() - 1 : 0) != recordDelimiter) {
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
