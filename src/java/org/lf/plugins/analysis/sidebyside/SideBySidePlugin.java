package org.lf.plugins.analysis.sidebyside;

import javax.swing.Icon;

import org.lf.logs.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.sidebyside.LogsPair;

import com.sun.istack.internal.Nullable;


public class SideBySidePlugin implements AnalysisPlugin {

	@Nullable
	public Class getOutputType(Class[] inputTypes) {
		if (inputTypes.length == 2 &&
            Log.class.isAssignableFrom(inputTypes[0]) &&
            Log.class.isAssignableFrom(inputTypes[1]))
        {
	        return LogsPair.class;
        }
		return null;
    }

    public Entity applyTo(Entity[] args) {
    	return new Entity(new Attributes(), new LogsPair(args[0], args[1]));
    }

    public String getName() {
        return "Show logs side by side";
    }

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
}
