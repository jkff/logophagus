package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.parser.Log;
import org.lf.plugins.DisplayPlugin;

public class ViewLogAsHighlightedTablePlugin implements DisplayPlugin {

	@Override
	public JComponent createView(Object data) {
		return new HighlightedScrollableLogTable((Log)data);
	}

	@Override
	public Class getInputType() {
		return Log.class;
	}

}
