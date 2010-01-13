package org.lf.plugins.analysis;

import javax.swing.JOptionPane;

import org.lf.parser.Log;
import org.lf.parser.FilteredLog;
import org.lf.plugins.AnalysisPlugin;
import org.lf.util.RecordFilter;

public class FilterBySubstringPlugin implements AnalysisPlugin{
    
    public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return Log.class;
    }

    public Object applyTo(Object[] args) {
        Log log = (Log) args[0];

        String substring = JOptionPane.showInputDialog(null, "Enter substring for filter", "Filter setup",JOptionPane.QUESTION_MESSAGE );
        if (substring == null)
        	return null;
        return new FilteredLog(log, new RecordFilter(substring));
    }

    public String getName() {
            return "LogFilter";  //To change body of implemented methods use File | Settings | File Templates.
    }


}
