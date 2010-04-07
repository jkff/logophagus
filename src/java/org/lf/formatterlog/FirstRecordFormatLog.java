package org.lf.formatterlog;

import java.io.IOException;

import org.lf.logs.Field;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;

public class FirstRecordFormatLog extends FormattedLog {
	private final Field[] fields;
	
	public FirstRecordFormatLog(Log log) throws IOException {
		super(log);
		this.fields = log.readRecord(log.first()).getFields();
	}

	@Override
	public Field[] getFields() {
		return fields;
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		Record originalRec = this.log.readRecord(pos);
		return originalRec;
	}

}
