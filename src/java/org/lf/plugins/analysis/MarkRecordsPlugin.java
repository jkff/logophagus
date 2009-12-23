package org.lf.plugins.analysis;

import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;

/**
 * User: jkff
 * Date: Dec 18, 2009
 * Time: 6:31:05 PM
 */
public class MarkRecordsPlugin implements AnalysisPlugin {
    public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return LogAndHighlighter.class;
    }

    public Object applyTo(Object[] args) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
