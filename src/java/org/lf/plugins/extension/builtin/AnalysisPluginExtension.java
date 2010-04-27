package org.lf.plugins.extension.builtin;

import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.extension.Extension;
import org.lf.plugins.extension.ExtensionID;

public abstract class AnalysisPluginExtension implements AnalysisPlugin, Extension {
    public final static ExtensionID ourID = new ExtensionID() {
        private final String name = AnalysisPluginExtension.class.getName();

        @Override
        public String getName() {
            return name;
        }
    };

    @Override
    public ExtensionID getID() {
        return ourID;
    }
}
