package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

public class ViewScrollableLogPlugin implements Plugin, DisplayPlugin {

    @Override
    public void init(ProgramContext context) {
        context.getDisplayPluginRepository().register(this);
    }

    @Override
    public View createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes);
    }

    @Override
    public boolean isApplicableFor(Object o) {
        return o instanceof Log;
    }
}
