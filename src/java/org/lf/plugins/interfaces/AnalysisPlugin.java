package org.lf.plugins.interfaces;

public interface AnalysisPlugin {
    Class[] getInputTypes();
    Class getOutputType();
    Object applyTo(Object[] args);
    String getName();
}