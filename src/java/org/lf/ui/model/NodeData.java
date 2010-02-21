package org.lf.ui.model;

import org.lf.plugins.Entity;

import javax.swing.JComponent;

public class NodeData {
	public final Entity entity;
	public final JComponent component;

    public NodeData(Entity entity, JComponent component){
		this.entity = entity;
		this.component = component;
	}
	
	public String toString() {
		return entity.data.toString();
	}
}
