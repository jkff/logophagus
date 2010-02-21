package org.lf.plugins.analysis.highlight;

import java.awt.Color;

import org.lf.parser.Record;

public interface RecordColorer {
	public Color getColor(Record r);
}
