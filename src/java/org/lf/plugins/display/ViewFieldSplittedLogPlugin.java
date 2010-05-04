package org.lf.plugins.display;

import org.lf.plugins.Entity;
import org.lf.plugins.analysis.splitbyfield.LogAndField;

import javax.swing.*;
//import org.lf.ui.components.plugins.fieldsplittedlog.FieldSplittedLog;

public class ViewFieldSplittedLogPlugin implements DisplayPlugin {

    @Override
    public JComponent createView(Entity entity) {
        throw new UnsupportedOperationException("Fix SplitByFieldValuesPlugin and include it into compilation again");
//        return new FieldSplittedLog((LogAndField)entity.data, entity.attributes);
    }

    @Override
    public boolean isApplicableFor(Object o) {
        return o != null &&
                LogAndField.class.isAssignableFrom(o.getClass());
    }

}
