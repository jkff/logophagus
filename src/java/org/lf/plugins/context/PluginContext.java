package org.lf.plugins.context;

import org.lf.plugins.extension.ExtensionPointsManager;

public class PluginContext {
    private final ExtensionPointsManager epm = new ExtensionPointsManager();
    private final TreeContext analysisPluginTreeContext;

    public PluginContext(TreeContext analysisPluginTreeContext) {
        this.analysisPluginTreeContext = analysisPluginTreeContext;
    }

    ExtensionPointsManager getExtensionPointsManager() {
        return epm;
    }

    TreeContext getAnalysisPluginTreeContext() {
        return analysisPluginTreeContext;
    }

}
