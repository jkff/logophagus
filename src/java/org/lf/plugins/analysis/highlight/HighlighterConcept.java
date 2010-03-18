package org.lf.plugins.analysis.highlight;

import org.lf.plugins.AttributeConcept;
import org.lf.plugins.AttributeInstance;

import java.util.Collection;
import java.util.List;

public class HighlighterConcept implements AttributeConcept<HighlighterConcept, Highlighter> {
    @Override
    public Highlighter join(Collection<Highlighter> parents) {
        return new Highlighter(parents);
    }

}
