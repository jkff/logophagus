package org.lf.plugins.analysis;

import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.LogsPair;

import com.sun.istack.internal.Nullable;


public class SideBySidePlugin implements AnalysisPlugin {

	@Nullable
	public Class getOutputType(Class[] inputTypes) {
		if( inputTypes.length == 2 && Log.class.isAssignableFrom(inputTypes[0]) && Log.class.isAssignableFrom(inputTypes[1])) 
	        return LogsPair.class; 
		return null;
    }

    public Entity applyTo(Entity[] args) {
    	return new Entity(new Attributes(), new LogsPair(args[0], args[1]));
    }

    public String getName() {
        return "SideBySideLogs";
    }
}
