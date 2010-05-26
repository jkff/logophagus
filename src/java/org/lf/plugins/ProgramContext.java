package org.lf.plugins;

import com.thoughtworks.xstream.XStream;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.TreePluginRepository;

/**
 * Created on: 26.05.2010 15:44:10
 */
public class ProgramContext {
    private TreePluginRepository treePluginRepository = new TreePluginRepository();
    private DisplayPluginRepository displayPluginRepository = new DisplayPluginRepository();
    private ExtensionPointsManager extensionPointsManager = new ExtensionPointsManager();
    private XStream xstream = new XStream();

    public ProgramContext() {
    }

    public ExtensionPointsManager getExtensionPointsManager() {
        return extensionPointsManager;
    }

    public XStream getXstream() {
        return xstream;
    }

    public TreePluginRepository getTreePluginRepository() {
        return treePluginRepository;
    }

    public DisplayPluginRepository getDisplayPluginRepository() {
        return displayPluginRepository;
    }
}
