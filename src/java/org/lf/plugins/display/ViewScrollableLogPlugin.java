package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import javax.swing.*;

public class ViewScrollableLogPlugin implements DisplayPlugin {

    public JComponent createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes);
    }

    public Class getInputType() {
        return Log.class;
    }

}
