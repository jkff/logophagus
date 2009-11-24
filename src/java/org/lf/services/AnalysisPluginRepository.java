package org.lf.services;

import java.util.*;

import org.lf.plugins.interfaces.AnalysisPlugin;

public class AnalysisPluginRepository {
    private static AnalysisPluginRepository instance;
    private List<AnalysisPlugin> analysisPlugins = new ArrayList<AnalysisPlugin>();

    public static AnalysisPluginRepository getInstance(){
        if (instance == null) {
            instance = new AnalysisPluginRepository();
        }
        return instance;
    }

    public void registerAnalysisPlugin(Class<? extends AnalysisPlugin> plugin) throws PluginException {
        try {
            analysisPlugins.add(plugin.newInstance());
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        }
    }

    private static boolean areCompatible(Class[] formalParamTypes, Object[] actualParams) {
    	if(formalParamTypes.length != actualParams.length)
            return false;
    	for(int i = 0; i < actualParams.length; ++i) {
            if(actualParams[i] != null && !formalParamTypes[i].isInstance(actualParams[i]))
                return false;
        }
        return true;
    }

    public List<AnalysisPlugin> getApplicableAnalysisPlugins(Object[] args) {
        List<AnalysisPlugin> res = new ArrayList<AnalysisPlugin>();
        for (AnalysisPlugin plugin : analysisPlugins) {
            if(areCompatible(plugin.getInputTypes(), args))
                res.add(plugin);
        }
        return res;
    }
}
