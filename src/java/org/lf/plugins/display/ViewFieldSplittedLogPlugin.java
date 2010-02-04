package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.services.LogAndField;
import org.lf.ui.components.plugins.fieldSplittedLog.FieldSplittedLog;

public class ViewFieldSplittedLogPlugin implements DisplayPlugin {

	@Override
	public JComponent createView(Entity entity) {		
		return new FieldSplittedLog((LogAndField)entity.data, entity.attributes);
	}

	@Override
	public Class getInputType() {
		return LogAndField.class;
	}

}
