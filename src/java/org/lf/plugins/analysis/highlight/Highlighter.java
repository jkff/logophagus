package org.lf.plugins.analysis.highlight;

import org.lf.logs.Record;
import org.lf.plugins.AttributeConcept;
import org.lf.plugins.AttributeInstance;

import com.sun.istack.internal.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Highlighter implements AttributeInstance<HighlighterConcept,Highlighter> {
    private Collection<Highlighter> parents;
    private RecordColorer colorer;

    public Highlighter(Collection<Highlighter> parents) {
        this.parents = parents;
    }

    public void setRecordColorer(RecordColorer rc) {
        this.colorer = rc;
    }

    @Nullable
    public Color getHighlightColor(Record rec) {
        if (colorer != null && colorer.getColor(rec) != null)
            return colorer.getColor(rec);
        for(Highlighter p : parents) {
            Color c = p.getHighlightColor(rec);
            if(c != null) return c;
        }
        return null;
    }

    @Override
    public Highlighter createChild() {
        return new Highlighter(Arrays.asList(this));
    }

    @Override
    public HighlighterConcept getConcept() {
        return new HighlighterConcept();
    }

}
