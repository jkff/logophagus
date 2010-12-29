package org.lf.logs;

import org.jetbrains.annotations.NotNull;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.lf.encoding.ScrollableReader;
import org.lf.encoding.UTF8Reader;
import org.lf.io.RandomAccessFileIO;
import org.lf.io.ScrollableInputStream;
import org.lf.parser.Parser;
import org.lf.parser.Position;
import org.lf.util.Pair;

import java.io.IOException;

public class FileBackedLog implements Log {
    private static final Chronology ISO_CHRONOLOGY = ISOChronology.getInstance();

    private final RandomAccessFileIO file;
    private final Parser parser;

    private Position cachedLast;
    private long maxOffsetAtCachedLast;

    private DateTime cachedTime;
    private Position posWithCachedTime;

    private boolean hasTimeField;

    private class PhysicalPosition implements Position {
        final long offsetBytes;

        @Override
        public int hashCode() {
            return (int) (offsetBytes ^ (offsetBytes >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null &&
                    obj.getClass() == PhysicalPosition.class &&
                    ((PhysicalPosition) obj).offsetBytes == this.offsetBytes;
        }

        PhysicalPosition(long offsetBytes) {
            this.offsetBytes = offsetBytes;
        }

        @Override
        public String toString() {
            return "Physical position " + offsetBytes;
        }

        @Override
        public Log getCorrespondingLog() {
            return FileBackedLog.this;
        }
    }

    public FileBackedLog(RandomAccessFileIO io, Parser in) throws IOException {
        this.parser = in;
        this.file = io;

        for (Format f : parser.getFormats()) {
            if (f.getTimeFieldIndex() != -1) {
                hasTimeField = true;
                break;
            }
        }

    }

    @Override
    public Position first() {
        return new PhysicalPosition(0L);
    }

    @Override
    public synchronized Position last() throws IOException {
        ScrollableInputStream sis = null;
        ScrollableReader reader;
        try {
            if (cachedLast == null || file.length() != maxOffsetAtCachedLast) {
                sis = file.getInputStreamFrom(file.length() - 1);
                reader = new UTF8Reader(sis);
                reader.scrollToEnd();
                cachedLast = prev(new PhysicalPosition(reader.getCurrentOffset()));
                maxOffsetAtCachedLast = reader.getCurrentOffset();
            }
            return  cachedLast;
        } finally {
            if (sis != null)
                sis.close();
        }
    }

    @Override
    public Record readRecord(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            is = file.getInputStreamFrom(((PhysicalPosition) pos).offsetBytes);
            ScrollableReader reader = new UTF8Reader(is);
            Record res = parser.readRecord(reader);
            return res;
        } finally {
            if (is != null)
                is.close();
        }
    }

    @Override
    public Position next(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            PhysicalPosition pp = (PhysicalPosition) pos;
            is = file.getInputStreamFrom(pp.offsetBytes);
            ScrollableReader reader = new UTF8Reader(is);
            long offset = parser.findNextRecord(reader);
            if (offset == -1) {
                return null;
            }
            return new PhysicalPosition(offset);
        } finally {
            if (is != null)
                is.close();
        }
    }

    @Override
    public Position prev(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            PhysicalPosition pp = (PhysicalPosition) pos;
            is = file.getInputStreamFrom(pp.offsetBytes);
            ScrollableReader reader = new UTF8Reader(is);
            long offset = parser.findPrevRecord(reader);
            if (offset == -1)
                return null;
            return new PhysicalPosition(offset);
        } finally {
            if (is != null)
                is.close();
        }
    }

    @Override
    public String toString() {
        return file.getFileName();
    }

    @Override
    public Position convertToNative(Position p) throws IOException {
        if (p.getCorrespondingLog() != this) return null;
        return p;
    }

    @Override
    public Pair<Log, Position> convertToParent(Position pos) throws IOException {
        return null;
    }

    @Override
    public Format[] getFormats() {
        return parser.getFormats();
    }

    @Override
    public synchronized DateTime getTime(Position pos) throws IOException {
        if (!hasTimeField) {
            return null;
        }

        if (pos == posWithCachedTime) {
            return cachedTime;
        }

        DateTime res = getTimeImpl(pos);
        posWithCachedTime = pos;
        cachedTime = res;

        return res;
    }

    @Override
    public Position findNearestBeforeTime(DateTime time) throws IOException {
        // Naive implementation. Use binary search afterwards.
        Position prev = null;
        for(Position p = first(); p != null; p = next(p)) {
            DateTime t = getTime(p);
            if(t == null)
                continue;
            if(t.isAfter(time))
                return prev;
            prev = p;
        }
        return null;
    }

    private DateTime getTimeImpl(@NotNull Position pos) throws IOException {
        Record posRecord = readRecord(pos);
        if (posRecord.getFormat().getTimeFieldIndex() != -1) {
            return getTimeFromRecord(posRecord);
        }

        Position cur = pos;
        while (cur != null && !cur.equals(first())) {
            cur = prev(cur);
            Record curRec = readRecord(cur);
            if (curRec.getFormat().getTimeFieldIndex() != -1) {
                return getTimeFromRecord(curRec);
            }
        }

        cur = pos;
        while (cur != null && !cur.equals(last())) {
            cur = next(cur);
            Record curRec = readRecord(cur);
            if (curRec.getFormat().getTimeFieldIndex() != -1) {
                return getTimeFromRecord(curRec);
            }
        }

        return null;
    }

    private DateTime getTimeFromRecord(Record rec) {
        DateTimeFormatter dtf = rec.getFormat().getTimeFormat();
        return new DateTime(
                dtf.parseMillis(rec.getCell(rec.getFormat().getTimeFieldIndex()).toString()), ISO_CHRONOLOGY);
    }

}