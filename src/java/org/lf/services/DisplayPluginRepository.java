package org.lf.services;

import org.lf.plugins.display.DisplayPlugin;
import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.util.Removable;

import java.util.ArrayList;
import java.util.List;

public class DisplayPluginRepository {
    public final static ExtensionPointID<DisplayPlugin> EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<DisplayPlugin> EXTENSION_POINT = new ExtensionPoint<DisplayPlugin>() {
        @Override
        public Removable addExtension(final DisplayPlugin extension) {
            register(extension);
            return new Removable() {
                @Override
                public void remove() {
                    DisplayPluginRepository.remove(extension);
                }
            };
        }
    };

    static {
        ExtensionPointsManager.registerExtensionPoint(EXTENSION_POINT_ID, EXTENSION_POINT);
    }

    private static List<DisplayPlugin> displayPlugins = new ArrayList<DisplayPlugin>();

    public static void register(DisplayPlugin plugin) {
        displayPlugins.add(plugin);
    }

    public static void remove(DisplayPlugin plugin) {
        displayPlugins.remove(plugin);
    }

    public static List<DisplayPlugin> getApplicablePlugins(Object arg) {
        List<DisplayPlugin> res = new ArrayList<DisplayPlugin>();
        for (DisplayPlugin plugin : displayPlugins) {
            if (plugin.isApplicableFor(arg))
                res.add(plugin);
        }
        return res;
    }
}
