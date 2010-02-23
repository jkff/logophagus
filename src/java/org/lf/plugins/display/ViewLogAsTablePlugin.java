package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.parser.Log;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.ui.components.plugins.scrollablelogtable.ScrollableLogTable;

public class ViewLogAsTablePlugin implements DisplayPlugin {

	public JComponent createView(Entity entity) {
        return new ScrollableLogTable((Log) entity.data, entity.attributes);
	}

	public Class getInputType() {
		return Log.class;
	}

}
