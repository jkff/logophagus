package org.lf.logs;

import java.io.IOException;

import org.lf.parser.LogMetadata;
import org.lf.parser.Position;
import org.lf.util.Filter;

import com.sun.istack.internal.Nullable;


public class FilteredLog implements Log {
    private final Filter<Record> filter;

    private final Log underlyingLog;

    public FilteredLog(Log underlyingLog, Filter<Record> filter) {
        this.filter = filter;
        this.underlyingLog = underlyingLog;
    }

    @Nullable
    private Position seek(Position pos, boolean isForward) throws IOException {
        Position borderPos = isForward ? underlyingLog.last() : underlyingLog.first();
        if (pos.equals(borderPos)) return null;
        pos = isForward ? underlyingLog.next(pos) : underlyingLog.prev(pos);
        while (true) {
            if (filter.accepts(underlyingLog.readRecord(pos))) return pos;
            if (pos.equals(borderPos))                            return null;
            pos = isForward ? underlyingLog.next(pos) : underlyingLog.prev(pos);
        }
    }

    @Override
    @Nullable
    public Position next(Position pos) throws  IOException {
        return seek(pos, true);
    }

    @Override
    @Nullable
    public Position prev(Position pos) throws IOException {
        return seek(pos, false);
    }

    @Override
    @Nullable
    public Position first() throws IOException {
        Position pos = underlyingLog.first();
        if (pos == null) return null;
        if (filter.accepts(underlyingLog.readRecord(pos)))
            return pos;
        return seek(pos, true);
    }

    @Override
    @Nullable
    public Position last() throws IOException {
        Position pos = underlyingLog.last();
        if (pos == null) return null;
        if (filter.accepts(underlyingLog.readRecord(pos)))
            return pos;
        return seek(pos, false);
    }

    @Override
    @Nullable
    public Record readRecord(Position pos) throws IOException {
        if (pos == null) return null;
        Record rec = underlyingLog.readRecord(pos);
        if (filter.accepts(rec))
            return rec;
        return readRecord(seek(pos, true));
    }

    public String toString(){
        return underlyingLog.toString() + " => filter : " + filter.toString();
    }

	@Override
	public LogMetadata getMetadata() {
		return underlyingLog.getMetadata();
	}
}
