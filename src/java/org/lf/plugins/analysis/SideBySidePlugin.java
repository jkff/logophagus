package org.lf.plugins.analysis;

import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.LogsPair;


public class SideBySidePlugin implements AnalysisPlugin {
    public Class[] getInputTypes() {
        return new Class[]{Log.class , Log.class}; 
    }

    public Class getOutputType() {
        return LogsPair.class; 
    }

    public Entity applyTo(Entity[] args) {
    	return new Entity(Attributes.NONE, new LogsPair((Log)args[0].data, (Log)args[1].data));
    }

    public String getName() {
        return "SideBySideLogs";
    }
}
