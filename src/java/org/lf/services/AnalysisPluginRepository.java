package org.lf.services;

import java.util.*;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;

public class AnalysisPluginRepository {
    private static List<AnalysisPlugin> analysisPlugins = new ArrayList<AnalysisPlugin>();

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
        List<AnalysisPlugin> res = new ArrayList<AnalysisPlugin>();
        for (AnalysisPlugin plugin : analysisPlugins) {
            if(areCompatible(plugin.getInputTypes(), args))
                res.add(plugin);
        }
        return res;
    }

    private static boolean areCompatible(Class[] formalParamTypes, Entity[] actualParams) {
        if(formalParamTypes.length != actualParams.length)
            return false;
        for(int i = 0; i < actualParams.length; ++i) {
            if(actualParams[i] != null && !formalParamTypes[i].isInstance(actualParams[i].data))
                return false;
        }
        return true;
    }
}
