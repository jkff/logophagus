package org.lf.plugins;

import java.util.List;

import static org.lf.util.CollectionFactory.newList;

/**
 * Created on: 26.05.2010 16:27:04
 */
public class PluginManager {
    private boolean isInitialized = false;
    private List<Plugin> plugins = newList();
    private ProgramContext context;

    public PluginManager(ProgramContext context) {
        this.context = context;
    }

    public void addPlugin(Plugin plugin) {
        if(isInitialized) {
            throw new IllegalStateException("Plugin manager is already initialized");
        }
        plugins.add(plugin);
    }

    public void init() {
        for(Plugin plugin : plugins) {
            plugin.init(context);
        }
    }
}
