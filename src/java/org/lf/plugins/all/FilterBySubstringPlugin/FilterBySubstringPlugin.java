package org.lf.plugins.all.FilterBySubstringPlugin;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.lf.parser.Log;
import org.lf.parser.FilteredLog;
import org.lf.parser.Record;
import org.lf.plugins.interfaces.AnalysisPlugin;
import org.lf.util.Filter;

public class FilterBySubstringPlugin implements AnalysisPlugin{
    
    public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return Log.class;
    }

    public Object applyTo(Object[] args) {
        Log log = (Log) args[0];

        final String substring = JOptionPane.showInputDialog(null, "Enter substring for filter", "Filter setup",JOptionPane.QUESTION_MESSAGE );
        if (substring == null)
        	return null;
        return new FilteredLog(new Filter<Record>()
                                {
                                    public boolean accepts(Record record) {
                                        return record.toString().contains(substring);
                                    }
                                    public String toString(){
                                        return substring;
                                    }
                                }
                                , log);
    }

    public String getName() {
            return "LogFilter";  //To change body of implemented methods use File | Settings | File Templates.
    }


}
