package org.lf.ui.components.plugins.scrollablelog;

import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.util.Pair;

import java.util.List;

/**
 * Created on: 27.05.2010 15:35:47
 */
public class ScrollableLogState {
    public final List<Pair<Position, Record>> shownContent;

    public ScrollableLogState(List<Pair<Position, Record>> shownContent) {
        this.shownContent = shownContent;
    }
}
