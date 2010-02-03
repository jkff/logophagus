package org.lf.plugins.display;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.services.LogsPair;
import org.lf.ui.components.plugins.ScrollableLogTable;

public class ViewSideBySidePlugin implements DisplayPlugin {

	public JComponent createView(Entity entity) {
        LogsPair p = (LogsPair) entity.data;
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.add(new ScrollableLogTable(p.first));
		panel.add(new ScrollableLogTable(p.second));
		return panel;
	}

	public Class getInputType() {
		return LogsPair.class;
	}

}
