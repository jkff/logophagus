package org.lf.logs;

import java.io.IOException;

import org.lf.parser.Position;
import org.lf.util.Filter;

import com.sun.istack.internal.Nullable;


public class FilteredLog implements Log {
	private final Filter<Record> filter;

	private final Log underlyingLog;

	private class FilteredPosition implements Position {
		private final Position underlyingPos;

		private FilteredPosition(Position underlyingPos) {
			this.underlyingPos = underlyingPos;
		}

		@Override
		public Log getCorrespondingLog() {
			return FilteredLog.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((underlyingPos == null) ? 0 : underlyingPos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FilteredPosition other = (FilteredPosition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (underlyingPos == null) {
				if (other.underlyingPos != null)
					return false;
			} else if (!underlyingPos.equals(other.underlyingPos))
				return false;
			return true;
		}

		private FilteredLog getOuterType() {
			return FilteredLog.this;
		}    	
	}
	
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
			if (pos.equals(borderPos))                         return null;
			pos = isForward ? underlyingLog.next(pos) : underlyingLog.prev(pos);
		}
	}

	@Override
	@Nullable
	public Position next(Position pos) throws  IOException {
		FilteredPosition fPos = (FilteredPosition)pos;
		Position res = seek(fPos.underlyingPos, true);
		return res == null ? null : new FilteredPosition(res);
	}

	@Override
	@Nullable
	public Position prev(Position pos) throws IOException {
		FilteredPosition fPos = (FilteredPosition)pos;
		Position res = seek(fPos.underlyingPos, false);
		return res == null ? null : new FilteredPosition(res);
	}

	@Override
	@Nullable
	public Position first() throws IOException {
		Position pos = underlyingLog.first();
		if (pos == null) return null;
		if (filter.accepts(underlyingLog.readRecord(pos)))
			return new FilteredPosition(pos);
		pos = seek(pos, true);
		if (pos != null) return new FilteredPosition(pos);
		return null;
	}

	@Override
	@Nullable
	public Position last() throws IOException {
		Position pos = underlyingLog.last();
		if (pos == null) return null;
		if (filter.accepts(underlyingLog.readRecord(pos)))
			return new FilteredPosition(pos);
		pos = seek(pos, false);
		if (pos != null) return new FilteredPosition(pos);
		return null;
	}

	@Override
	@Nullable
	public Record readRecord(Position pos) throws IOException {
		if (pos == null) return null;
		FilteredPosition fPos = (FilteredPosition)pos;
		Record rec = underlyingLog.readRecord(fPos.underlyingPos);
		if (filter.accepts(rec))
			return rec;
		return null;
	}

	public String toString(){
		return underlyingLog.toString() + " => filter : " + filter.toString();
	}


	//find first position that accepts filter and that equals to input pos or after it
	//so method invocation can take a lot of time
	@Override
	public Position convertToNative(Position pos) throws IOException {
		if (pos == null) return null; 
		if (pos.getCorrespondingLog() == this) return pos;
		if (pos.getCorrespondingLog() != this.underlyingLog) return null;
		if (filter.accepts(underlyingLog.readRecord(pos)))
			return new FilteredPosition(pos);
		Position res = seek(pos,true);
		if (res != null) return new FilteredPosition(res);
		res = seek(pos, false);
		if (res != null) return new FilteredPosition(res);
		return null;
	}
}
