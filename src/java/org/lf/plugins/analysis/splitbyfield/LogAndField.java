package org.lf.plugins.analysis.splitbyfield;

import org.lf.parser.Log;

public class LogAndField {
	public final Log log;
	public final int field;
	
	public LogAndField(Log log, int field) {
		this.log = log;
		this.field = field;
	}
	
	public String toString(){
		return log.toString() + " => split by field " + field;
	}
}
