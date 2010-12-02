package org.lf.logs;

public interface Record {
    public int getCellCount();

    public CharSequence getCell(int i);

    public Format getFormat();

    public CharSequence getRawString();
}
