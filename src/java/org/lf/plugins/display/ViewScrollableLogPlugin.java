package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import javax.swing.*;

public class ViewScrollableLogPlugin implements DisplayPlugin {
    @Override
    public JComponent createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes);
    }

    @Override
    public boolean isApplicableFor(Object o) {
        return o instanceof Log;
    }
}
