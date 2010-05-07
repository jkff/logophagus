package org.lf.plugins.analysis;

import com.sun.istack.internal.Nullable;
import org.lf.ui.components.tree.TreeContext;

import javax.swing.*;


// TODO Rename to TreePlugin
public interface AnalysisPlugin {
    @Nullable
    TreeAction getActionFor(TreeContext context);

    String getName();

    Icon getIcon();
}