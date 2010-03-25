package org.lf.plugins.analysis.splitbyfield;

import org.lf.logs.Log;


public class LogAndField {
    public final Log log;
    public final int fieldIndex;

    public LogAndField(Log log, int fieldIndex) {
        this.log = log;
        this.fieldIndex = fieldIndex;
    }

    public String toString(){
        return log.toString() + " => split by " + fieldIndex + " field" ;
    }
}
