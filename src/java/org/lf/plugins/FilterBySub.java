package org.lf.plugins;


public class FilterBySub implements AnalysisPlugin{


    public Class[] getInputTypes() {
        return new Class[]{String.class};  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getOutputType() {
        return String.class;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object applyTo(Object[] args) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
