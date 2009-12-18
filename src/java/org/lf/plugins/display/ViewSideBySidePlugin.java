package org.lf.plugins.display;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lf.plugins.DisplayPlugin;
import org.lf.services.LogsPair;

public class ViewSideBySidePlugin implements DisplayPlugin {

	public JComponent createView(Object data) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.add(new ScrollableLogTable(((LogsPair)data).first));
		panel.add(new ScrollableLogTable(((LogsPair)data).second));
		return panel;
	}

	public Class getInputType() {
		return LogsPair.class;
	}

}
