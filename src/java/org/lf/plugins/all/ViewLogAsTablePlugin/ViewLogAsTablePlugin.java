package org.lf.plugins.all.ViewLogAsTablePlugin;

import javax.swing.JComponent;

import org.lf.parser.Log;
import org.lf.plugins.interfaces.DisplayPlugin;
import org.lf.util.ScrollableLogTable;

public class ViewLogAsTablePlugin implements DisplayPlugin {

	public JComponent createView(Object data) {
		return new ScrollableLogTable((Log)data);
	}

	public Class getInputType() {
		return Log.class;
	}

}
