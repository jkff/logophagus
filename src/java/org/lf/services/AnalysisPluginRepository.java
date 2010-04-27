package org.lf.services;

import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;

import java.util.LinkedList;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class AnalysisPluginRepository {
    private static List<AnalysisPlugin> analysisPlugins = newList();

    public static void register(AnalysisPlugin plugin) {
        analysisPlugins.add(plugin);
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
