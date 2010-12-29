package org.lf.parser.regex;

import org.lf.encoding.ScrollableReader;
import org.lf.logs.*;
import org.lf.parser.Parser;
import org.lf.util.CharVector;
import org.lf.util.Triple;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpParser implements Parser {
    private final Format[] regexpFormats;
    private final Pattern[] patterns;
    private final int[] linesPerRecord;
    private int maxLinesPerRecord = 1;

    private transient ScrollableReader cachedReader;
//
    private transient long cachedOffset;

    // Long - absolute offset, Integer - index of pattern that matched (-1 means no pattern matched),
    // Object is a Matcher if a pattern matched, or a CharSequence (raw input line) if none matched
    private Triple<Long, Integer, Object> cachedOffsetIndexMatch;

    public RegexpParser(String[] regexps, Format[] regexpFormats, int[] linesPerRecord) {
        this.patterns = new Pattern[regexps.length];
        for (int i = 0; i < regexps.length; ++i) {
            patterns[i] = Pattern.compile(regexps[i]);
        }

        this.linesPerRecord = linesPerRecord;

        for (int cur : linesPerRecord) {
            if (cur > maxLinesPerRecord)
                maxLinesPerRecord = cur;
        }

        this.regexpFormats = regexpFormats;
    }

    @Override
    public long findNextRecord(ScrollableReader reader) throws IOException {
        return findOIMforward(reader).first;
    }

    private synchronized Triple<Long, Integer, Object> findOIMforward(ScrollableReader reader) throws IOException {
        if (!reader.isSameSource(cachedReader) || reader.getCurrentOffset() != cachedOffset) {
            cachedReader = reader;
            cachedOffset = reader.getCurrentOffset();
            cachedOffsetIndexMatch = getRecordFromCharStream(reader, true);
        }
        return cachedOffsetIndexMatch;
    }

    @Override
    public long findPrevRecord(ScrollableReader reader) throws IOException {
        // scroll back to \n symbol
        if (reader.prev() == -1) return -1;
        return getRecordFromCharStream(reader, false).first;
    }

    @Override
    public Record readRecord(ScrollableReader reader) throws IOException {
        Triple<Long, Integer, Object> offsetIndexMatch = findOIMforward(reader);

        if (offsetIndexMatch.second == -1) {
            return new LineRecord((CharSequence) offsetIndexMatch.third);
        }

        return new RegexRecord((Matcher) offsetIndexMatch.third, regexpFormats[offsetIndexMatch.second]);
    }

    private Triple<Long, Integer, Object> getRecordFromCharStream(ScrollableReader reader, boolean isForward) throws IOException {
        long nextLineOffset = 0;
        CharSequence firstLineString = null;
        CharVector curLines = new CharVector(256);

        for (int numLines = 1; numLines <= maxLinesPerRecord; ++numLines) {
            CharSequence line = isForward ? reader.readForwardUntil('\n') : reader.readBackwardUntil('\n');

            int numRs = 0;
            for(int i = 0; i < line.length(); ++i) {
                if(line.charAt(i) == '\r') {
                    numRs++;
                }
            }

            if(numRs > 0) {
                char[] cleanLine = new char[line.length() - numRs];
                for(int i = 0, j = 0; i < line.length(); ++i) {
                    char chr = line.charAt(i);
                    if(chr != '\r')
                        cleanLine[j++] = chr;
                }
                if (isForward)
                    curLines.add(cleanLine);
                else
                    curLines.add(cleanLine, 0, cleanLine.length);
            } else {
                if (isForward)
                    curLines.add(line);
                else
                    curLines.add(0, line);
            }


            if (isForward)
                nextLineOffset = reader.getCurrentOffset();
            else if (curLines.charAt(0) == '\n')  {
                reader.next();
                nextLineOffset = reader.getCurrentOffset();
                reader.prev();
            }

            if (numLines == 1) {
                firstLineString = curLines.subSequence(0, curLines.length());
            }

            for (int i = 0; i < linesPerRecord.length; ++i) {
                if (linesPerRecord[i] != numLines) continue;

                int toNewline = curLines.length();
                if(toNewline > 0 && curLines.charAt(toNewline - 1) == '\n')
                    --toNewline;

                Matcher m = patterns[i].matcher(curLines.subSequence(0, toNewline));

                if (m.matches()) {
                    if (!isForward && curLines.charAt(0) == '\n') {
                        reader.next();
                        return new Triple<Long, Integer, Object>(reader.getCurrentOffset(), i, m);
                    }
                    return new Triple<Long, Integer, Object>(reader.getCurrentOffset(), i, m);
                }

            }
            // if there is no recordDelimiter at the joined lines scrollToEnd then the scrollToBegin/scrollToEnd of file occurs
            // and we can't continue to read next lines 
            if (curLines.charAt(isForward ? curLines.length() - 1 : 0) != '\n') {
                break;
            }
        }
        return new Triple<Long, Integer, Object>(nextLineOffset, -1, firstLineString);
    }

    @Override
    public Format[] getFormats() {
        return regexpFormats;
    }
}
