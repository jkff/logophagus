package org.lf.plugins.analysis.merge;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.lf.logs.Log;
import org.lf.logs.TimeMergeLogs;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.ProgramProperties;


public class MergeLogsPlugin implements AnalysisPlugin {

    @Override
    public Entity applyTo(Entity[] args) {
        Log[] logs = new Log[args.length];
        for (int i = 0; i < args.length; ++i ) {
            logs[i] = (Log) args[i].data;
        }

        Log mergedLog = null;
        try {
            mergedLog = new TimeMergeLogs(logs);

            Attributes[] childAttributes = new Attributes[args.length];
            for (int i = 0 ; i < args.length ; ++i) {
                childAttributes[i] = args[i].attributes;
            }
            
            Attributes myAttributes = Attributes.join(childAttributes, mergedLog);
            if (mergedLog != null)
                return new Entity(myAttributes, mergedLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        return TimeMergeLogs.class;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath +"multi.gif");
    }

}
