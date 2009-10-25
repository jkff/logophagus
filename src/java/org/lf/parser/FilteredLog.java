package org.lf.parser;

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

	public Position next(Position pos) throws  Exception {
		return seekForward(pos);
	}

	public Position prev(Position pos) throws Exception {
		return seekBackward(pos);
	}

	private Position seekForward(Position pos) throws Exception {
		Position temp = pos;
		while (true){
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
	private Position seekBackward(Position pos) throws Exception {
		Position temp = pos;
		while (true){
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

	public Position getStart() throws Exception {
		return seekForward(underlyingLog.getStart());
	}

	public Position getEnd() throws Exception {
		return seekBackward(underlyingLog.getEnd());
	}

	public Record readRecord(Position pos) throws Exception {
		return underlyingLog.readRecord(pos);
	}
}
