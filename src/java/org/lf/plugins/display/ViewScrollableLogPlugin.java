package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.ui.components.plugins.scrollablelog.extension.builtin.BookmarkExtension;
import org.lf.ui.components.plugins.scrollablelog.extension.builtin.SearchExtension;

public class ViewScrollableLogPlugin implements DisplayPlugin {

    static {
        SearchExtension se = new SearchExtension();
        BookmarkExtension be = new BookmarkExtension();
        ExtensionPoint<SLInitExtension> ep = ExtensionPointsManager.getExtensionPoint(
                ScrollableLogPanel.SL_INIT_EXTENSION_POINT_ID);
        ep.addExtension(se);
        ep.addExtension(be);
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
