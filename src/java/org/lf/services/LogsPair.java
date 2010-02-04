package org.lf.services;

import org.lf.plugins.Entity;

public class LogsPair{
	final public Entity first;
	final public Entity second;
	public LogsPair(Entity first, Entity  second){
		this.first = first;
		this.second = second;
	}
	
	public String toString(){
		return first.toString()+" && "+second.toString();
	}
}
