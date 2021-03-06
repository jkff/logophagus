package org.lf.ui.components.plugins.scrollablelog;

import org.jetbrains.annotations.Nullable;
import org.lf.logs.Log;
import org.lf.plugins.Attributes;
import org.lf.plugins.display.View;
import org.lf.ui.components.tree.PluginTree;

import javax.swing.*;

/**
 * Created on: 26.05.2010 15:02:01
 */
public class ScrollableLogView implements View<ScrollableLogState> {
    private Log log;
    private Attributes attributes;

    private ScrollableLogPanel panel;

    public ScrollableLogView(Log log, Attributes attributes, @Nullable PluginTree pluginTree) {
        this.panel = new ScrollableLogPanel(log, attributes, pluginTree);
        this.log = log;
        this.attributes = attributes;
    }

    public Log getLog() {
        return log;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public ScrollableLogState getState() {
        return panel.getState();
    }

    public void restoreState(ScrollableLogState state) {
        panel.restoreState(state);
    }
}
