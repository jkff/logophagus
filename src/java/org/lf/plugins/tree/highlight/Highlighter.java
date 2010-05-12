package org.lf.plugins.tree.highlight;

import com.sun.istack.internal.Nullable;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.AttributeInstance;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

public class Highlighter implements AttributeInstance<HighlighterConcept, Highlighter> {
    private final Collection<Highlighter> parents;
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
        if (parents == null)
            return null;
        for (Highlighter p : parents) {
            Color c = p.getHighlightColor(rec);
            if (c != null) return c;
        }
        return null;
    }

    @Override
    public HighlighterConcept getConcept() {
        return new HighlighterConcept();
    }

    @Override
    public Highlighter createChild(Log attributeOwner) {
        return new Highlighter(Arrays.asList(this));
    }

}
