package org.lf.services;

import org.lf.plugins.display.DisplayPlugin;

import java.util.ArrayList;
import java.util.List;

public class DisplayPluginRepository {
    private static List<DisplayPlugin> displayPlugins = new ArrayList<DisplayPlugin>();

    public void register(DisplayPlugin plugin) {
        displayPlugins.add(plugin);
    }

    public List<DisplayPlugin> getApplicablePlugins(Object arg) {
        List<DisplayPlugin> res = new ArrayList<DisplayPlugin>();
        for (DisplayPlugin plugin : displayPlugins) {
            if (plugin.isApplicableFor(arg))
                res.add(plugin);
        }
        return res;
    }
}
