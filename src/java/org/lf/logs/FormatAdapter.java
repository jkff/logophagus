package org.lf.logs;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FormatAdapter extends Format {
    private final Field[] fields;
    private final int timeIndex;
    private final DateTimeFormatter timeFormat;
    
    public FormatAdapter(Field[] fields, int timeIndex, DateTimeFormatter timeFormat) {
        this.fields = fields;
        this.timeFormat = timeFormat;
        this.timeIndex = timeIndex;
    }
    
    @Override
    public Field[] getFields() {
        return fields;
    }
    
    @Override
    public int getTimeFieldIndex() {
        return timeIndex;
    }
    @Override
    public DateTimeFormatter getTimeFormat() {
        return timeFormat;
    }

}
