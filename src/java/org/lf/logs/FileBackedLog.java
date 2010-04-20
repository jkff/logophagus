package org.lf.logs;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import org.lf.parser.Parser;
import org.lf.parser.Position;
import org.lf.parser.ScrollableInputStream;

import java.io.IOException;

public class FileBackedLog implements Log {
    private final RandomAccessFileIO file;
    private final Parser parser;

    private Position cachedLast;
    private long fileLengthAtCachedLast;

    private DateTime cachedTime;
    private Position posWithCachedTime;
    private boolean hasTimeField;


    private class PhysicalPosition implements Position {
        final long offsetBytes;

        @Override
        public int hashCode() {
            return (int)(offsetBytes ^ (offsetBytes >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null &&
            obj.getClass() == PhysicalPosition.class &&
            ((PhysicalPosition)obj).offsetBytes == this.offsetBytes;
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

    public FileBackedLog(String fileName, Parser in) throws IOException {
        this(new MappedFile(fileName), in);
    }

    public FileBackedLog(RandomAccessFileIO io, Parser in) throws IOException {
        this.parser = in;
        this.file = io;

        for(Format f: parser.getFormats()) {
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
        long len = file.length();
        if(cachedLast == null || len != fileLengthAtCachedLast) {
            cachedLast = prev(new PhysicalPosition(len));
            fileLengthAtCachedLast = len;
        }
        return cachedLast;
    }

    @Override
    public Record readRecord(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            is = file.getInputStreamFrom(((PhysicalPosition) pos).offsetBytes);
            return parser.readRecord(is);
        } finally {
            if (is != null) is.close();
        }
    }

    @Override
    public Position next(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            PhysicalPosition pp = (PhysicalPosition) pos;
            is = file.getInputStreamFrom(pp.offsetBytes);
            long offset = parser.findNextRecord(is);
            if (offset == 0)
                return null;
            return new PhysicalPosition(pp.offsetBytes + offset);
        } finally {
            if (is != null) is.close();
        }
    }

    @Override
    public Position prev(Position pos) throws IOException {
        ScrollableInputStream is = null;
        try {
            PhysicalPosition pp = (PhysicalPosition) pos;
            is = file.getInputStreamFrom(pp.offsetBytes);
            long offset = parser.findPrevRecord(is);
            if (offset == 0)
                return null;
            return new PhysicalPosition(pp.offsetBytes - offset);
        } finally {
            if (is != null) is.close();
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
    public Format[] getFormats() {
        return parser.getFormats();
    }

    @Override
    public synchronized DateTime getTime(Position pos) throws IOException {
        if (!hasTimeField) return null;
        
        if(pos == posWithCachedTime) {
            return cachedTime;
        }

        DateTime res = getTimeImpl(pos);
        posWithCachedTime = pos;
        cachedTime = res;

        return res;
    }

    private DateTime getTimeImpl(Position pos) throws IOException {

        Record posRecord = readRecord(pos);
        if (posRecord.getFormat().getTimeFieldIndex() != -1) {
            DateTimeFormatter dtf = posRecord.getFormat().getTimeFormat();
            return dtf.parseDateTime(posRecord.getCellValues()[posRecord.getFormat().getTimeFieldIndex()]);
        }

        Position cur = pos;
        while (!cur.equals(first())) {
            cur = prev(cur);
            Record curRec = readRecord(cur);
            if (curRec.getFormat().getTimeFieldIndex() != -1) {
                DateTimeFormatter dtf = curRec.getFormat().getTimeFormat();
                return dtf.parseDateTime(curRec.getCellValues()[curRec.getFormat().getTimeFieldIndex()]);
            }
        }

        cur = pos;
        while (!cur.equals(last())) {
            cur = next(cur);
            Record curRec = readRecord(cur);
            if (curRec.getFormat().getTimeFieldIndex() != -1) {
                DateTimeFormatter dtf = curRec.getFormat().getTimeFormat();
                return dtf.parseDateTime(curRec.getCellValues()[curRec.getFormat().getTimeFieldIndex()]);
            }
        }

        return null;
    }

}