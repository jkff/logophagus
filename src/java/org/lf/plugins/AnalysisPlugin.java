package org.lf.plugins;

public interface AnalysisPlugin {
    Class[] getInputTypes();
    Class getOutputType();
    Object applyTo(Object[] args);
    String getName();
}