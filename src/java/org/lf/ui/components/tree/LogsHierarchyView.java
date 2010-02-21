package org.lf.ui.components.tree;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTree;

import org.lf.ui.model.LogsHierarchy;



public class LogsHierarchyView extends JTree implements Observer {
	
	public LogsHierarchyView(LogsHierarchy logsHierarchy) {
		super(logsHierarchy.getTreeModel());
		logsHierarchy.addObserver(this);
		this.setAutoscrolls(true);
		this.setScrollsOnExpand(true);
	}

	@Override
	public void update(Observable o, Object arg) {
		this.updateUI();
	}
	
}
