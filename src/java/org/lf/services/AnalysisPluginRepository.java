package org.lf.services;

import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.util.Removable;

import java.util.LinkedList;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class AnalysisPluginRepository {
    public final static ExtensionPointID<AnalysisPlugin> EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<AnalysisPlugin> EXTENSION_POINT = new ExtensionPoint<AnalysisPlugin>() {
        @Override
        public Removable addExtension(final AnalysisPlugin extension) {
            AnalysisPluginRepository.register(extension);
            return new Removable() {
                @Override
                public void remove() {
                    analysisPlugins.remove(extension);
                }
            };
        }
    };

    static {
        ExtensionPointsManager.registerExtensionPoint(EXTENSION_POINT_ID, EXTENSION_POINT);
    }

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
