package org.lf.services;

import org.lf.parser.Record;
import org.lf.plugins.Attribute;

import com.sun.istack.internal.Nullable;

import java.awt.*;

/**
 * User: jkff
 * Date: Dec 18, 2009
 * Time: 6:35:47 PM
 */
public class Highlighter implements Attribute<Highlighter> {
	private Highlighter parent; 
	private RecordColorer colorer;
	
	public Highlighter(Highlighter parent) {
		this.parent = parent;
	}

	public void setRecordColorer(RecordColorer rc) {
		this.colorer = rc;
	}

	@Nullable
	public Color getHighlightColor(Record rec) {
		if (colorer != null && colorer.getColor(rec) != null)
			return colorer.getColor(rec);
		if (parent == null)
			return null;
		return parent.getHighlightColor(rec);
	}

    @Override
    public Highlighter getParent() {
        return parent;
    }

    @Override
	public Highlighter createChild() {
		return new Highlighter(this);
	}

}
