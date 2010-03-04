package org.lf.plugins;

import com.sun.istack.internal.Nullable;

import javax.swing.*;


public interface AnalysisPlugin {
    @Nullable
    Class getOutputType(Class[] inputTypes);

    @Nullable
    Entity applyTo(Entity[] args);
    String getName();

    Icon getIcon();
}