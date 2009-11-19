package org.lf.plugins;

import org.lf.parser.Log;

public class FilterBySubstringPlugin implements  AnalysisPlugin{

    public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return Log.class;
    }

    public Object applyTo(Object[] args) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return "LogFilter";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
