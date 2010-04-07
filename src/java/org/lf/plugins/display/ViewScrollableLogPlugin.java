package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.logs.Log;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

public class ViewScrollableLogPlugin implements DisplayPlugin {

    public JComponent createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes);
    }

    public Class getInputType() {
        return Log.class;
    }

}
