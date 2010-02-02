package org.lf.plugins.analysis;

import org.jetbrains.annotations.Nullable;
import org.lf.parser.Record;
import org.lf.plugins.Attributes;

import java.awt.*;

/**
 * User: jkff
 * Date: Dec 18, 2009
 * Time: 6:35:47 PM
 */
public interface Highlighter {
    Attributes.Combiner<Highlighter> COMBINE_SEQUENTIALLY = new Attributes.Combiner<Highlighter>() {
        public Highlighter combine(final Highlighter a, final Highlighter b) {
            return new Highlighter() {
                public Color getHighlightColor(Record rec) {
                    Color fromB = b.getHighlightColor(rec);
                    return fromB == null ? a.getHighlightColor(rec) : fromB; 
                }
            };
        }
    };

    @Nullable
    Color getHighlightColor(Record rec);
}
