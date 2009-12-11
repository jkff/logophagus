package org.lf.plugins.analysis;

import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.services.LogsPair;


public class SideBySidePlugin implements AnalysisPlugin{
    public Class[] getInputTypes() {
        return new Class[]{Log.class , Log.class}; 
    }

    public Class getOutputType() {
        return LogsPair.class; 
    }

    public Object applyTo(Object[] args) {
    	return new LogsPair((Log)args[0] , (Log)args[1]);
    }

    public String getName() {
        return "SideBySideLogs";
    }
}
