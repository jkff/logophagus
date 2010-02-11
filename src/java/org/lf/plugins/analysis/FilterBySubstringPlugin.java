package org.lf.plugins.analysis;

import javax.swing.JOptionPane;

import org.lf.parser.Log;
import org.lf.parser.FilteredLog;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.util.RecordFilter;

import com.sun.istack.internal.Nullable;

public class FilterBySubstringPlugin implements AnalysisPlugin {
    @Nullable
	public Class getOutputType(Class[] inputTypes) {
    	if ( inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0])) 
			return Log.class;
		return null;
    }

    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;

        String substring = JOptionPane.showInputDialog(null, "Enter substring for filter", "Filter setup",JOptionPane.QUESTION_MESSAGE );
        if (substring == null)
        	return null;
        return new Entity(args[0].attributes.createSuccessor(), new FilteredLog(log, new RecordFilter(substring)));
    }

    public String getName() {
        return "Filter by substring";
    }


}
