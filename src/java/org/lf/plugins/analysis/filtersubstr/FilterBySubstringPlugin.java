package org.lf.plugins.analysis.filtersubstr;

import com.sun.istack.internal.Nullable;
import org.lf.logs.FilteredLog;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.services.ProgramProperties;
import org.lf.util.Filter;

import javax.swing.*;

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

            public boolean accepts(Record r) {
                for (Object cell : r.getCellValues()) {
                    if (cell != null && ((String) cell).contains(substring))
                        return true;
                }
                return false;
            }
        };

        Log fLog = new FilteredLog(log, filter);
        return new Entity(args[0].attributes.createSuccessor(fLog), fLog);
    }

    public String getName() {
        return "Filter by substring";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "filter.gif");
    }


}
