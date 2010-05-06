package org.lf.ui.components.tree;

import org.lf.plugins.Entity;

import javax.swing.*;

public class NodeData {
    public final Entity entity;
    public JComponent component;
    public final Icon icon;

    public NodeData(Entity entity, Icon icon) {
        this(entity, null, icon);
    }

    public NodeData(Entity entity, JComponent component, Icon icon) {
        this.entity = entity;
        this.component = component;
        this.icon = icon;
    }

    public String toString() {
        return entity.data.toString();
    }
}
