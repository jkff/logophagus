package org.lf.services;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;

import java.util.LinkedList;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class AnalysisPluginRepository {
    private static List<AnalysisPlugin> analysisPlugins = newList();

    public static void register(Class<? extends AnalysisPlugin> plugin) throws PluginException {
        try {
            analysisPlugins.add(plugin.newInstance());
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        }
    }

    public static List<AnalysisPlugin> getApplicablePlugins(Entity[] args) {
        List<Class> pluginArgsList = new LinkedList<Class>();
        for (Entity entity : args) {
            pluginArgsList.add(entity.data.getClass());
        }

        Class[] pluginArgsArray = pluginArgsList.toArray(new Class[0]);

        List<AnalysisPlugin> res = newList();
        for (AnalysisPlugin plugin : analysisPlugins) {
            if (plugin.getOutputType(pluginArgsArray) != null)
                res.add(plugin);
        }

        return res;
    }
}
