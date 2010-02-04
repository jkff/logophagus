package org.lf.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.util.Filter;

public class Bookmarks {
	private Bookmarks parent;
	private Map<String, Position> data = new LinkedHashMap<String, Position>();

    public static Attributes.Combiner<Bookmarks> COMBINE_BOOKMARK = new Attributes.Combiner<Bookmarks>() {
        public Bookmarks combine(Bookmarks a, Bookmarks b) {
            b.parent = a;
            return b;
        }
    };

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
    	
}
