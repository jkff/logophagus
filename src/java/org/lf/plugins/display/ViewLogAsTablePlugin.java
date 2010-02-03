package org.lf.plugins.display;

import javax.swing.JComponent;

import org.lf.parser.Log;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.services.Highlighter;
import org.lf.ui.components.plugins.ScrollableLogTable;

public class ViewLogAsTablePlugin implements DisplayPlugin {

	public JComponent createView(Entity entity) {
        ScrollableLogTable table = new ScrollableLogTable((Log) entity.data);
        table.setHighlighter(entity.attributes.getValue(Highlighter.class));
        return table;
	}

	public Class getInputType() {
		return Log.class;
	}

}
