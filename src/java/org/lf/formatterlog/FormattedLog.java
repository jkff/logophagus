package org.lf.formatterlog;

import java.io.IOException;

import org.lf.logs.Field;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;

public abstract class FormattedLog implements Log {
	protected final Log log;
	
	public FormattedLog(Log log) {
		super();
		this.log = log;
	}

	@Override
	public final Position convertToNative(Position pos) throws IOException {
		return log.convertToNative(pos);
	}

	@Override
	public final Position first() throws IOException {
		return log.first();
	}

	@Override
	public final Position last() throws IOException {
		return log.last();
	}

	@Override
	public final Position next(Position pos) throws IOException {
		return log.next(pos);
	}

	@Override
	public final Position prev(Position pos) throws IOException {
		return log.prev(pos);
	}

	/**
	 * @return
	 * - record that fits FormattedLog format e.g. it has all fields that return getFields() method.
	 * - null, if record don't fit format 
	 */
	@Override
	public abstract Record readRecord(Position pos) throws IOException;

	public Record readRawRecord(Position pos) throws IOException {
		return log.readRecord(pos);
	}
		
	public abstract Field[] getFields();
}
