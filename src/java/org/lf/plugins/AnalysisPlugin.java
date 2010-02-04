package org.lf.plugins;

import org.jetbrains.annotations.Nullable;

public interface AnalysisPlugin {
    @Nullable
    Class getOutputType(Class[] inputTypes);

    @Nullable
    Entity applyTo(Entity[] args);
    String getName();
}