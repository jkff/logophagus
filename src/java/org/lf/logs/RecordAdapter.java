package org.lf.logs;

public class RecordAdapter implements Record {
	private final String[] cells;
	private final Format format;
	
	public RecordAdapter(String[] cells, Format format) {
		this.cells = cells;
		this.format = format;
	}
	
	@Override
	public String[] getCellValues() {
		return this.cells;
	}

	@Override
	public Format getFormat() {
		return this.format;
	}

}
