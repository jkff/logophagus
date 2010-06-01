package org.lf.logs;

/**
 * Created on: 01.06.2010 23:50:04
 */
public class LineRecord implements Record {
    private String value;

    public LineRecord(String value) {
        this.value = value;
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    @Override
    public String getCell(int i) {
        if(i != 0)
            throw new IndexOutOfBoundsException("Cell index too large: " + i + " > 0");
        return value;
    }

    @Override
    public Format getFormat() {
        return Format.UNKNOWN_FORMAT;
    }
}
