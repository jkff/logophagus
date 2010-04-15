package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
//import org.lf.ui.components.plugins.fieldsplittedlog.FieldSplittedLog;

public class ViewFieldSplittedLogPlugin implements DisplayPlugin {

    @Override
    public JComponent createView(Entity entity) {
        throw new UnsupportedOperationException("Fix SplitByFieldValuesPlugin and include it into compilation again");
//        return new FieldSplittedLog((LogAndField)entity.data, entity.attributes);
    }

    @Override
    public Class getInputType() {
        return LogAndField.class;
    }

}
