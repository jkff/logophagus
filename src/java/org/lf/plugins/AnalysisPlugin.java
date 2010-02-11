package org.lf.plugins;

import com.sun.istack.internal.Nullable;


public interface AnalysisPlugin {
    @Nullable
    Class getOutputType(Class[] inputTypes);

    @Nullable
    Entity applyTo(Entity[] args);
    String getName();
}