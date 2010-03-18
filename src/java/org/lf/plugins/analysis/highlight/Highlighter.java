package org.lf.plugins.analysis.highlight;

import org.lf.logs.Record;
import org.lf.plugins.AttributeConcept;
import org.lf.plugins.AttributeInstance;

import com.sun.istack.internal.Nullable;

import java.awt.*;

public class Highlighter implements AttributeInstance<HighlighterConcept> {
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

    @Override
    public AttributeConcept<HighlighterConcept> getConcept() {
        return new HighlighterConcept();
    }

}
