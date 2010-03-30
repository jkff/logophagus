package org.lf.plugins.display;


import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lf.logs.Log;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.sidebyside.LogsPair;
import org.lf.ui.components.plugins.scrollablelogtable.ScrollableLogView;

public class ViewSideBySidePlugin implements DisplayPlugin {

    public JComponent createView(Entity entity) {
        LogsPair p = (LogsPair) entity.data;
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        panel.add(new ScrollableLogView((Log)p.first.data, p.first.attributes));
        panel.add(Box.createHorizontalStrut(12));
        panel.add(new ScrollableLogView((Log)p.second.data, p.second.attributes));
        
        return new JScrollPane(panel);
    }

    public Class getInputType() {
        return LogsPair.class;
    }

}
