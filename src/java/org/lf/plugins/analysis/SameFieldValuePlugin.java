package org.lf.plugins.analysis;

import org.lf.parser.FilteredLog;
import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.util.FieldFilter;

public class SameFieldValuePlugin implements AnalysisPlugin {
	private String value; 
	private int index; 
	
    public SameFieldValuePlugin(int index, String value) {
    	this.index = index;
    	this.value = value;
    }
    
	public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return Log.class;
    }

    public Object applyTo(Object[] args) {
        Log log = (Log) args[0];
        return new FilteredLog(log, new FieldFilter(index ,value));
    }

    public String getName() {
            return "SameFieldLogFilter"; 
    }
}
