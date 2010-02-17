package org.lf.parser;

import java.io.IOException;

import org.lf.util.Filter;


/**
 * User: jkff Date: Oct 6, 2009 Time: 10:33:15 AM
 */
public class FilteredLog implements Log {
	private final Filter<Record> filter;

	private final Log underlyingLog;

	public FilteredLog(Log underlyingLog, Filter<Record> filter) {
		this.filter = filter;
		this.underlyingLog = underlyingLog;
	}

	public Position next(Position pos) throws  IOException {
		return seekForward(pos);
	}

	public Position prev(Position pos) throws IOException {
		return seekBackward(pos);
	}

	private Position seekForward(Position pos) throws IOException {
		while (true) {
            pos = underlyingLog.next(pos);
            if(pos.equals(underlyingLog.last()) || filter.accepts(readRecord(pos)))
                return pos;
        }	
	}
	
	private Position seekBackward(Position pos) throws IOException {
		Position temp = pos;
		while (true){
			if (pos.equals(underlyingLog.prev(pos)))
				return temp;		
			pos = underlyingLog.prev(pos);
			if (filter.accepts(readRecord(pos)))
				return pos;
		}
	}

	public Position first() throws IOException {
		return seekForward(underlyingLog.first());
	}

	public Position last() throws IOException {
		return seekBackward(underlyingLog.last());
	}

	public Record readRecord(Position pos) throws IOException {
		return underlyingLog.readRecord(pos);
	}

	public String toString(){
		return underlyingLog.toString() + " => filter : " + filter.toString();
	}
}
