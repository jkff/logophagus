package org.lf.logs;

public class CompositeRecord implements Record {
    private final String[] cells;
    private final Format format;

    public CompositeRecord(String[] cells, Format format) {
        this.cells = cells;
        this.format = format;
    }

    @Override
    public int getCellCount() {
        return this.cells.length;
    }

    @Override
    public String getCell(int i) {
        return this.cells[i];
    }

    @Override
    public Format getFormat() {
        return this.format;
    }

}
