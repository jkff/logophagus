package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;

/**
 * Created on: 27.05.2010 16:02:10
 */
public class BookmarksPlugin implements Plugin {
    @Override
    public void init(ProgramContext context) {
        ExtensionPointsManager epm = context.getExtensionPointsManager();
        ExtensionPoint<SLInitExtension> ep = epm.getExtensionPoint(
                ScrollableLogPlugin.SL_INIT_EXTENSION_POINT_ID);

        ep.addExtension(new BookmarkExtension());
    }
}
