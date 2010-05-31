package org.lf.plugins.tree.highlight;

import org.lf.logs.Log;
import org.lf.plugins.AttributeConcept;

import java.util.Collection;

public class HighlighterConcept implements AttributeConcept<HighlighterConcept, Highlighter> {
    @Override
    public Highlighter join(Collection<Highlighter> parents, Log attributeOwner) {
        return new Highlighter(parents);
    }

}
