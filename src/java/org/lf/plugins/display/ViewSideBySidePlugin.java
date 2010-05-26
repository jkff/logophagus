package org.lf.plugins.display;


import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.tree.sidebyside.LogsPair;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import javax.swing.*;

public class ViewSideBySidePlugin implements DisplayPlugin {
    @Override
    public View createView(Entity entity) {
        LogsPair p = (LogsPair) entity.data;
        return new SideBySideView(
                new ScrollableLogView((Log) p.first.data, p.first.attributes),
                new ScrollableLogView((Log) p.second.data, p.second.attributes));
    }

    public boolean isApplicableFor(Object o) {
        return o instanceof LogsPair;
    }

}
