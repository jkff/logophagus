package org.lf.logs;

public class FormatAdapter extends Format {
    private final Field[] fields;
    private final int timeIndex;
    private final String timeFormat;
    
    public FormatAdapter(Field[] fields, int timeIndex, String timeFormat) {
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
    public String getTimeFormat() {
        return timeFormat;
    }

}
