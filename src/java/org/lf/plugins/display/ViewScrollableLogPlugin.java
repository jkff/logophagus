package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.tree.PluginTree;

public class ViewScrollableLogPlugin implements Plugin, DisplayPlugin {
    private PluginTree pluginTree;

    @Override
    public void init(ProgramContext context) {
        context.getDisplayPluginRepository().register(this);
        pluginTree = context.getPluginTree();
    }

    @Override
    public View createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes, pluginTree);
    }

    @Override
    public boolean isApplicableFor(Object o) {
        return o instanceof Log;
    }
}
