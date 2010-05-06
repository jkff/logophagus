package org.lf.plugins.analysis;

import com.sun.istack.internal.Nullable;
import org.lf.ui.components.tree.TreeContext;

import javax.swing.*;


public interface AnalysisPlugin {
    @Nullable
    TreeAction getActionFor(TreeContext context);

    String getName();

    Icon getIcon();
}