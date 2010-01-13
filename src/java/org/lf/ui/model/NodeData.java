package org.lf.ui.model;

import javax.swing.JComponent;

public class NodeData {
	public final Object data;
	public final JComponent jComponent;

	public NodeData(Object data, JComponent jComponent){
		this.data = data;
		this.jComponent = jComponent;
	}
	
	public String toString() {
		return data.toString();
	}
}
