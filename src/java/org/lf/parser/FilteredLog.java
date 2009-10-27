package org.lf.parser;

import java.io.IOException;

import org.lf.util.Filter;


/**
 * User: jkff Date: Oct 6, 2009 Time: 10:33:15 AM
 */
public class FilteredLog implements Log {
	private final Filter<Record> filter;

	private final Log underlyingLog;

	public FilteredLog(Filter<Record> filter, Log underlyingLog) {
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
		Position temp = pos;
		while (true){
            //readRecord(pos) finds next record just after pos and read bytes between them
			//if it's impossible to find next record then readRecors falls
			//thats why we need double check
			if (!pos.equals(underlyingLog.next(pos))){
				pos = underlyingLog.next(pos);
				if (!pos.equals(underlyingLog.next(pos))){
					if (filter.accepts(readRecord(pos))){
						return pos;
					}
				}
			} else {
				return temp;
			}
			
		}
	}
	private Position seekBackward(Position pos) throws IOException {
		Position temp = pos;
		while (true){
            //no double check(look at previous comment)
			if (!pos.equals(underlyingLog.prev(pos))){
				pos = underlyingLog.prev(pos);
				if (filter.accepts(readRecord(pos))){
					return pos;
				}
			} else {
				return temp;
			}
			
		}
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
