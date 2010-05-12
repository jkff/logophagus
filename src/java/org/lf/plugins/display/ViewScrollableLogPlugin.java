package org.lf.plugins.display;

import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.plugins.extension.ExtensionTracer;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLPluginsRepository;
import org.lf.ui.components.plugins.scrollablelog.extension.builtin.BookmarkExtension;
import org.lf.ui.components.plugins.scrollablelog.extension.builtin.SearchExtension;

import javax.swing.*;

public class ViewScrollableLogPlugin implements DisplayPlugin {

    static {
        final SearchExtension se = new SearchExtension();
        final BookmarkExtension be = new BookmarkExtension();
        ExtensionPoint ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.TOOLBAR_EXTENSION_POINT_ID);
        if (ep != null) {
            ep.addExtension(se);
            ep.addExtension(be);
        } else
            ExtensionPointsManager.addExtensionTracer(new ExtensionTracer() {
                @Override
                public void extensionAdd(ExtensionPointID id) {
                    if (id == SLPluginsRepository.TOOLBAR_EXTENSION_POINT_ID) {
                        ExtensionPoint ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.TOOLBAR_EXTENSION_POINT_ID);
                        ep.addExtension(se);
                        ep.addExtension(be);
                        ExtensionPointsManager.removeExtensionTracer(this);
                    }
                }
            });

        ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.KEY_LISTENER_EXTENSION_POINT_ID);
        if (ep != null) {
            ep.addExtension(se);
            ep.addExtension(be);
        } else
            ExtensionPointsManager.addExtensionTracer(new ExtensionTracer() {
                @Override
                public void extensionAdd(ExtensionPointID id) {
                    if (id == SLPluginsRepository.KEY_LISTENER_EXTENSION_POINT_ID) {
                        ExtensionPoint ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.KEY_LISTENER_EXTENSION_POINT_ID);
                        ep.addExtension(se);
                        ep.addExtension(be);
                        ExtensionPointsManager.removeExtensionTracer(this);
                    }
                }
            });

        ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.POPUP_EXTENSION_POINT_ID);
        if (ep != null)
            ep.addExtension(be);
        else
            ExtensionPointsManager.addExtensionTracer(new ExtensionTracer() {
                @Override
                public void extensionAdd(ExtensionPointID id) {
                    if (id == SLPluginsRepository.POPUP_EXTENSION_POINT_ID) {
                        ExtensionPoint ep = ExtensionPointsManager.getExtensionPoint(SLPluginsRepository.POPUP_EXTENSION_POINT_ID);
                        ep.addExtension(be);
                        ExtensionPointsManager.removeExtensionTracer(this);
                    }
                }
            });


    }

    @Override
    public JComponent createView(Entity entity) {
        return new ScrollableLogView((Log) entity.data, entity.attributes);

    }

    @Override
    public boolean isApplicableFor(Object o) {
        return o instanceof Log;
    }
}
