package org.lf.logs;

import java.util.Arrays;

import org.joda.time.format.DateTimeFormatter;

public class Format {
    private final Field[] fields;
    private final int timeIndex;
    private final DateTimeFormatter timeFormat;
    
    public static final Format UNKNOWN_FORMAT = new Format(new Field[] { new Field("UNKNOWN_FORMAT") }, -1 ,null);
    
    public Format(Field[] fields, int timeIndex, DateTimeFormatter timeFormat) {
        this.fields = fields;
        this.timeFormat = timeFormat;
        this.timeIndex = timeIndex;
    }
    
    public Field[] getFields() {
        return fields;
    }
    
    /**
    *
    * @return index in getFields() corresponding to time field
    * or -1 if format has no time field
    */
    public int getTimeFieldIndex() {
        return timeIndex;
    }

    /**
    *
    * @return pattern for matching time with "Joda Time" library or null if this format has no time field
    * For example "yyyy-MM-dd"
    */
    public DateTimeFormatter getTimeFormat() {
        return timeFormat;
    }

    @Override
    public boolean equals(Object obj) {
        return  obj != null                                     &&
                obj.getClass().isAssignableFrom(Format.class)   &&
                ((Format)obj).timeIndex == this.timeIndex       &&
                Arrays.equals(((Format)obj).fields, this.getFields());
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(fields);
    }


}
