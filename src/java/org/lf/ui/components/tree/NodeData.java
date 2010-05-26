package org.lf.ui.components.tree;

import org.lf.plugins.Entity;
import org.lf.plugins.display.View;

import javax.swing.*;

public class NodeData {
    public final Entity entity;
    public View view;
    public final String iconFilename;

    public NodeData(Entity entity, String iconFilename) {
        this(entity, null, iconFilename);
    }

    public NodeData(Entity entity, View view, String iconFilename) {
        this.entity = entity;
        this.view = view;
        this.iconFilename = iconFilename;
    }

    public String toString() {
        return entity.data.toString();
    }
}
