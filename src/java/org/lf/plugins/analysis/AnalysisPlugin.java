package org.lf.plugins.analysis;

import com.sun.istack.internal.Nullable;
import org.lf.plugins.Entity;

import javax.swing.*;


public interface AnalysisPlugin {
    @Nullable
    Class getOutputType(Class[] inputTypes);

    @Nullable
    Entity applyTo(Entity[] args);

    String getName();

    Icon getIcon();
}