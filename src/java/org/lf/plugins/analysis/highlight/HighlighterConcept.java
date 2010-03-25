package org.lf.plugins.analysis.highlight;

import org.lf.plugins.AttributeConcept;

import java.util.Collection;

public class HighlighterConcept implements AttributeConcept<HighlighterConcept, Highlighter> {
    @Override
    public Highlighter join(Collection<Highlighter> parents) {
        return new Highlighter(parents);
    }

}
