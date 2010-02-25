package org.lf.parser;

import java.io.IOException;

import org.lf.util.Filter;

import com.sun.istack.internal.Nullable;


public class FilteredLog implements Log {
	private final Filter<Record> filter;

	private final Log underlyingLog;

	public FilteredLog(Log underlyingLog, Filter<Record> filter) {
		this.filter = filter;
		this.underlyingLog = underlyingLog;
	}

	public Position next(Position pos) throws  IOException {
		return seek(pos, true);
	}

	public Position prev(Position pos) throws IOException {
		return seek(pos, false);
	}

	

	@Nullable
	private Position seek(Position pos, boolean isForward) throws IOException {
		Position borderPos = isForward ? underlyingLog.last() : underlyingLog.first();
		if (pos.equals(borderPos)) return null;
		pos = isForward ? underlyingLog.next(pos) : underlyingLog.prev(pos);
		while (true) {
			if (filter.accepts(readRecord(pos))) return pos;
			if (pos.equals(borderPos)) 	         return null;
			pos = isForward ? underlyingLog.next(pos) : underlyingLog.prev(pos);
		}	
	}

	public Position first() throws IOException {
		Position pos = underlyingLog.first();
		if (pos == null) return null; 
		if (filter.accepts(readRecord(pos)))
			return pos; 
		return seek(pos, true);
	}

	public Position last() throws IOException {
		Position pos = underlyingLog.last();
		if (pos == null) return null; 
		if (filter.accepts(readRecord(pos)))
			return pos; 
		return seek(pos, false);
	}

	public Record readRecord(Position pos) throws IOException {
		return underlyingLog.readRecord(pos);
	}

	public String toString(){
		return underlyingLog.toString() + " => filter : " + filter.toString();
	}
}
