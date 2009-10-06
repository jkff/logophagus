package org.lf.parser;

import org.lf.util.Filter;

import java.io.IOException;

/**
 * User: jkff
 * Date: Oct 6, 2009
 * Time: 10:33:15 AM
 */
public class FilteredLog implements Log {
    private final Filter<Record> filter;
    private final Log underlyingLog;

    public FilteredLog(Filter<Record> filter, Log underlyingLog) {
        this.filter = filter;
        this.underlyingLog = underlyingLog;
    }

    public Position next(Position pos) throws IOException {
        return seekForward(underlyingLog.next(pos));
    }

    public Position prev(Position pos) throws IOException {
        return seekBackward(underlyingLog.prev(pos));
    }

    private Position seekForward(Position pos) throws IOException {
        while(!filter.accepts(readRecord(pos)))
            pos = underlyingLog.next(pos);
        return pos;
    }

    private Position seekBackward(Position pos) throws IOException {
        while(!filter.accepts(readRecord(pos)))
            pos = underlyingLog.prev(pos);
        return pos;
    }

    public Position getStart() throws IOException {
        return seekForward(underlyingLog.getStart());
    }

    public Position getEnd() throws IOException {
        return seekBackward(underlyingLog.getEnd());
    }

    public Record readRecord(Position pos) throws IOException {
        return underlyingLog.readRecord(pos);
    }
}
