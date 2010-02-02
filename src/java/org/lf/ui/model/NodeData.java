package org.lf.ui.model;

import org.lf.plugins.Entity;

import javax.swing.JComponent;

public class NodeData {
	public final Entity entity;
	public final JComponent jComponent;

    public NodeData(Entity entity, JComponent jComponent){
		this.entity = entity;
		this.jComponent = jComponent;
	}
	
	public String toString() {
		return entity.toString();
	}
}
