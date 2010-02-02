package org.lf.plugins;

public interface AnalysisPlugin {
    Class[] getInputTypes();
    Class getOutputType();
    Entity applyTo(Entity[] args);
    String getName();
}