package org.lf.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lf.parser.Position;
import org.lf.plugins.Attribute;

public class Bookmarks implements Attribute{
	private Bookmarks parent;
	private Map<String, Position> data = new LinkedHashMap<String, Position>();

	public Bookmarks(Bookmarks parent) {
		this.parent = parent;
	}
		
	
	public List<String> getNames() {
		ArrayList<String> result = new ArrayList<String>(data.keySet());
		if (parent != null)
			result.addAll(parent.getNames());
		return result;
	}
	
	public Position getValue(String name) {
		if (data.containsKey(name))
			return data.get(name);
		if (parent == null)
			return null;
		return parent.getValue(name);
	}

	public int getSize() {
		return data.size() + (parent == null ? 0 : parent.getSize());
	}

	public boolean addBookmark(String name, Position pos) {
		if (data.containsKey(name))
			return false;
		data.put(name, pos);
		return true;
	}

	@Override
	public Attribute createSuccessor() {
		return new Bookmarks(this);
	}
    	
}
