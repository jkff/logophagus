package org.lf.plugins.analysis.filtersubstr;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.lf.logs.FilteredLog;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.util.Filter;

import com.sun.istack.internal.Nullable;

public class FilterBySubstringPlugin implements AnalysisPlugin {
    @Nullable
    public Class getOutputType(Class[] inputTypes) {
        if (inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0]))
            return Log.class;
        return null;
    }

    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;

        String substring = JOptionPane.showInputDialog(null,
                "Enter substring for filter", "Filter setup", JOptionPane.QUESTION_MESSAGE);
        if (substring == null)
            return null;
        final String sub = substring;
        Filter<Record> filter = new Filter<Record>() {
            private String substring = sub;

            public String toString() {
                return substring;
            }

            public boolean accepts(Record t) {
                for (int i = 0; i < t.size(); ++i) {
                    if (t.get(i).contains(substring))
                        return true;
                }
                return false;
            }
        };
        return new Entity(
                args[0].attributes.createSuccessor(), 
                new FilteredLog(log, filter));
    }

    public String getName() {
        return "Filter by substring";
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }


}
