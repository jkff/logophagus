package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.parser.Log;
import org.lf.plugins.DisplayPlugin;

public class ViewLogAsTablePlugin implements DisplayPlugin {

	public JComponent createView(Object data) {
		return new ScrollableLogTable((Log)data);
	}

	public Class getInputType() {
		return Log.class;
	}

}
