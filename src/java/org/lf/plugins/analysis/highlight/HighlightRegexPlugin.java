package org.lf.plugins.analysis.highlight;

import com.sun.istack.internal.Nullable;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class HighlightRegexPlugin implements AnalysisPlugin {

    @Nullable
    public Class getOutputType(Class[] inputTypes) {
        if (inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0]))
            return Log.class;
        return null;
    }

    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;

        final String regex = JOptionPane.showInputDialog(
                null, "Enter a regular expression to highlight", "Highlight setup",
                JOptionPane.QUESTION_MESSAGE);
        if (regex == null)
            return null;

        Attributes atr = args[0].attributes.createSuccessor(log);

        Highlighter highlighter = atr.getValue(Highlighter.class);
        if (highlighter == null) {
            highlighter = new Highlighter(null);
            atr.addAttribute(highlighter);
        }

        highlighter.setRecordColorer(new RecordColorer() {
            // Compile the pattern just once to avoid
            // recompiling at each record
            private final Pattern p = Pattern.compile(regex);

            @Override
            public Color getColor(Record r) {
                for (Object cell : r.getCellValues()) {
                    if (cell != null && p.matcher((String) cell).find()) return Color.RED;
                }
                return null;
            }
        });

        return new Entity(atr, log);
    }

    public String getName() {
        return "Highlight records matching regex";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "colorized.gif");
    }
}
