package org.lf.plugins.tree;

import org.jetbrains.annotations.Nullable;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.HierarchicalAction;

import javax.swing.*;

public interface TreePlugin {
    @Nullable
    HierarchicalAction getActionFor(TreeContext context);

    String getName();

    String getIconFilename();
}