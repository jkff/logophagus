package org.lf.plugins.analysis.merge;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.lf.logs.Log;
import org.lf.logs.MergeLogs;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.ProgramProperties;

public class MergeLogsPlugin implements AnalysisPlugin {

    @Override
    public Entity applyTo(Entity[] args) {
        Log[] logs = new Log[args.length];
        Integer[] fields = new Integer[args.length];

        for (int i = 0; i < args.length; ++i ) {
            final String index = JOptionPane.showInputDialog(null, "Enter field index of log (\"" + args[i].data + "\") for merging with other logs", "Merge setup", JOptionPane.QUESTION_MESSAGE );
            if (index == null)
                return null;

            logs[i] = (Log) args[i].data;
            fields[i] = Integer.parseInt(index);
        }
        Attributes[] allAttributes = new Attributes[args.length];
        for (int i=0; i < args.length; ++i) {
            allAttributes[i] = args[i].attributes;
        }

        return new Entity(Attributes.join(allAttributes),
                new MergeLogs(logs, fields));
    }

    @Override
    public String getName() {
        return "Merge logs";
    }

    @Override
    public Class getOutputType(Class[] inputTypes) {
        if( inputTypes.length <= 1) return null;
        for (Class clazz : inputTypes) {
            if (!Log.class.isAssignableFrom(clazz)) return null;
        }
        return MergeLogs.class;
    }

    @Override
    public Icon getIcon() {
    	return new ImageIcon(ProgramProperties.iconsPath +"multi.gif");
    }

}
