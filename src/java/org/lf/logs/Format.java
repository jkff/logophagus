package org.lf.logs;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class Format {
    
    public static final Format UNKNOWN_FORMAT = new Format() {
        private final Field[] fields = new Field[] {
          new Field("Unknown field")  
        };
        
        @Override
        public Field[] getFields() {
            return fields;
        }

        @Override
        public int getTimeFieldIndex() {
            return -1;
        }

        @Override
        public DateTimeFormatter getTimeFormat() {
            return null;
        }
    };
    
    public abstract Field[] getFields();

    /**
     *
     * @return index in getFields() corresponding to time field
     * or -1 if format has no time field
     */

    public abstract int getTimeFieldIndex();

    /**
     *
     * @return pattern for matching time with "Joda Time" library or null if this format has no time field
     * For example "yyyy-MM-dd"
     */
    public abstract DateTimeFormatter getTimeFormat();


    @Override
    public boolean equals(Object obj) {
        return  obj != null                                     &&
                obj.getClass().isAssignableFrom(Format.class)   &&
                ((Format)obj).getFields().equals(getFields());
    }


    @Override
    public int hashCode() {
        return getFields().hashCode();
    }


}
