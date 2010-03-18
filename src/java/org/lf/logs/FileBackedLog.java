package org.lf.logs;

import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import org.lf.parser.Parser;
import org.lf.parser.Position;
import org.lf.parser.ScrollableInputStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileBackedLog implements Log {
    private final RandomAccessFileIO file;
    private final Parser parser;
    private final ScrollableInputStream is;
    private Field[] fields;

    private static class PhysicalPosition implements Position {
        long offsetBytes;

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
    }

    public FileBackedLog(String fileName, Parser in) throws IOException {
        this.parser = in;
        this.file = new MappedFile(fileName);
        this.is = file.getInputStreamFrom(0L);
        fillInFieldsFromRecord(readRecord(first()));
    }

    @Override
    public Position first() {
        return new PhysicalPosition(0L);
    }

    @Override
    public Position last() throws IOException {
        return prev(new PhysicalPosition(file.length()));
    }

    @Override
    synchronized public Record readRecord(Position pos) throws IOException {
        is.scrollTo(((PhysicalPosition) pos).offsetBytes);
        return parser.readRecord(is);
    }

    @Override
    synchronized public Position next(Position pos) throws IOException {
        PhysicalPosition pp = (PhysicalPosition) pos;
        is.scrollTo(pp.offsetBytes);
        long offset = parser.findNextRecord(is);
        if (offset == 0)
            return null;
        return new PhysicalPosition(pp.offsetBytes + offset);
    }

    @Override
    synchronized public Position prev(Position pos) throws IOException {
        PhysicalPosition pp = (PhysicalPosition) pos;
        is.scrollTo(pp.offsetBytes);
        long offset = parser.findPrevRecord(is);
        if (offset == 0)
            return null;
        return new PhysicalPosition(pp.offsetBytes - offset);
    }

    @Override
    public String toString() {
        return file.getFileName();
    }

    @Override
    public Field[] getFields() {
        return fields;
    }

    private void fillInFieldsFromRecord(Record rec) {
        this.fields = new Field[rec.size()];

        for (int i=0; i<rec.size(); ++i) {
            final int finalI = i;
            fields[i] = new Field() {
                @Override
                public Type getType() {
                    return Type.TEXT;
                }

                @Override
                public String getName() {
                    return "Field " + finalI;
                }
            };
        }
    }

}