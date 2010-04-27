package org.lf.services;

import org.lf.plugins.display.DisplayPlugin;

import java.util.ArrayList;
import java.util.List;

public class DisplayPluginRepository {
    private static List<DisplayPlugin> displayPlugins = new ArrayList<DisplayPlugin>();

    public static void register(Class<? extends DisplayPlugin> plugin) throws PluginException {
        try {
            displayPlugins.add(plugin.newInstance());
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        }
    }

    public static List<DisplayPlugin> getApplicablePlugins(Object arg) {
        List<DisplayPlugin> res = new ArrayList<DisplayPlugin>();
        for (DisplayPlugin plugin : displayPlugins) {
            if (plugin.getInputType().isInstance(arg))
                res.add(plugin);
        }
        return res;
    }
}
