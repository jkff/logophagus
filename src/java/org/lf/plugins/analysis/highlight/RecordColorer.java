package org.lf.plugins.analysis.highlight;

import org.lf.logs.Record;

import java.awt.*;

public interface RecordColorer {
    public Color getColor(Record r);
}
