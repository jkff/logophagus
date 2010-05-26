package org.lf.ui.components.plugins.scrollablelog;

import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ListExtensionPoint;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;

import java.util.List;

/**
 * Created on: 26.05.2010 15:49:11
 */
public class ScrollableLogPlugin implements Plugin {
    public static final ExtensionPointID<SLInitExtension> SL_INIT_EXTENSION_POINT_ID = ExtensionPointID.create();

    private static ListExtensionPoint<SLInitExtension> extensionPoint = new ListExtensionPoint<SLInitExtension>();

    @Override
    public void init(ProgramContext context) {
        context.getExtensionPointsManager().registerExtensionPoint(SL_INIT_EXTENSION_POINT_ID, extensionPoint);
    }

    static List<SLInitExtension> getInitExtensions() {
        return extensionPoint.getItems();
    }
}
