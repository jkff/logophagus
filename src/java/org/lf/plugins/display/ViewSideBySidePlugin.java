package org.lf.plugins.display;


import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.sidebyside.LogsPair;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import javax.swing.*;

public class ViewSideBySidePlugin implements DisplayPlugin {
    @Override
    public JComponent createView(Entity entity) {
        LogsPair p = (LogsPair) entity.data;
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        panel.add(new ScrollableLogView((Log) p.first.data, p.first.attributes));
        panel.add(Box.createHorizontalStrut(12));
        panel.add(new ScrollableLogView((Log) p.second.data, p.second.attributes));

        return new JScrollPane(panel);
    }

    public boolean isApplicableFor(Object o) {
        return o != null &&
                LogsPair.class.isAssignableFrom(o.getClass());
    }

}
