package org.lf.logs;

public interface Record {
    public int getCellCount();

    public String getCell(int i);

    public Format getFormat();
}
