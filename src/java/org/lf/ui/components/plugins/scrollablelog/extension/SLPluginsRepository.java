package org.lf.ui.components.plugins.scrollablelog.extension;

import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.util.Removable;

import java.util.Collection;

import static org.lf.util.CollectionFactory.newList;


public class SLPluginsRepository {
    private final static Collection<SLKeyListener> KEY_LISTENERS = newList();
    private final static Collection<SLToolbarExtension> TOOLBAR_EXTENSIONS = newList();
    private final static Collection<SLPopupExtension> POPUP_EXTENSIONS = newList();

    public final static ExtensionPointID<SLKeyListener> KEY_LISTENER_EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<SLKeyListener> KEY_LISTENER_EXTENSION_POINT = new ExtensionPoint<SLKeyListener>() {
        @Override
        public Removable addExtension(final SLKeyListener extension) {
            KEY_LISTENERS.add(extension);
            return new Removable() {
                @Override
                public void remove() {
                    KEY_LISTENERS.remove(extension);
                }
            };
        }
    };

    public final static ExtensionPointID<SLToolbarExtension> TOOLBAR_EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<SLToolbarExtension> TOOLBAR_EXTENSION_POINT = new ExtensionPoint<SLToolbarExtension>() {
        @Override
        public Removable addExtension(final SLToolbarExtension extension) {
            TOOLBAR_EXTENSIONS.add(extension);
            return new Removable() {
                @Override
                public void remove() {
                    TOOLBAR_EXTENSIONS.remove(extension);
                }
            };
        }
    };

    public final static ExtensionPointID<SLPopupExtension> POPUP_EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<SLPopupExtension> POPUP_EXTENSION_POINT = new ExtensionPoint<SLPopupExtension>() {
        @Override
        public Removable addExtension(final SLPopupExtension extension) {
            POPUP_EXTENSIONS.add(extension);
            return new Removable() {
                @Override
                public void remove() {
                    POPUP_EXTENSIONS.remove(extension);
                }
            };
        }
    };

    static {
        ExtensionPointsManager.registerExtensionPoint(KEY_LISTENER_EXTENSION_POINT_ID, KEY_LISTENER_EXTENSION_POINT);
        ExtensionPointsManager.registerExtensionPoint(TOOLBAR_EXTENSION_POINT_ID, TOOLBAR_EXTENSION_POINT);
        ExtensionPointsManager.registerExtensionPoint(POPUP_EXTENSION_POINT_ID, POPUP_EXTENSION_POINT);
    }

    public static SLKeyListener[] getRegisteredKeyListeners() {
        return KEY_LISTENERS.toArray(new SLKeyListener[0]);
    }

    public static SLToolbarExtension[] getRegisteredToolbarExtensions() {
        return TOOLBAR_EXTENSIONS.toArray(new SLToolbarExtension[0]);
    }

    public static SLPopupExtension[] getRegisteredPopupExtensions() {
        return POPUP_EXTENSIONS.toArray(new SLPopupExtension[0]);
    }

}
