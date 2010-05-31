package org.lf.plugins.tree.splitbyfield;

import org.lf.logs.Field;
import org.lf.logs.Log;


public class LogAndField {
    public final Log log;
    public final Field field;

    public LogAndField(Log log, Field field) {
        this.log = log;
        this.field = field;
    }

    public String toString() {
        return log.toString() + " => split by " + field + " field";
    }
}
