package org.lf.plugins.display;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lf.parser.Log;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.sidebyside.LogsPair;
import org.lf.ui.components.plugins.scrollablelogtable.ScrollableLogView;

public class ViewSideBySidePlugin implements DisplayPlugin {

	public JComponent createView(Entity entity) {
        LogsPair p = (LogsPair) entity.data;
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.add(new ScrollableLogView((Log)p.first.data, p.first.attributes));
		panel.add(new ScrollableLogView((Log)p.second.data, p.second.attributes));
		return panel;
	}

	public Class getInputType() {
		return LogsPair.class;
	}

}
