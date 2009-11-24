package org.lf.services;

import java.util.*;

import org.lf.plugins.interfaces.AnalysisPlugin;
import org.lf.plugins.interfaces.DisplayPlugin;

public class DisplayPluginRepository {
    private static DisplayPluginRepository instance;
    private List<DisplayPlugin> displayPlugins = new ArrayList<DisplayPlugin>();

    public static DisplayPluginRepository getInstance(){
        if (instance == null) {
            instance = new DisplayPluginRepository();
        }
        return instance;
    }

    public void registerDisplayPlugin(Class<? extends DisplayPlugin> plugin) throws PluginException {
        try {
            displayPlugins.add(plugin.newInstance());
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        }
    }

    public List<DisplayPlugin> getApplicableDisplayPlugins(Object arg) {
        List<DisplayPlugin> res = new ArrayList<DisplayPlugin>();
        for (DisplayPlugin plugin : displayPlugins) {
            if(plugin.getInputType().isInstance(arg))
                res.add(plugin);
        }
        return res;
    }
}
