package org.lf.plugins.analysis;

import org.jetbrains.annotations.Nullable;
import org.lf.parser.Record;

import java.awt.*;

/**
 * User: jkff
 * Date: Dec 18, 2009
 * Time: 6:35:47 PM
 */
public interface Highlighter {
    @Nullable
    Color getHighlightColor(Record rec);
}
