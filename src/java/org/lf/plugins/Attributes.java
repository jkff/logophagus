package org.lf.plugins;

import com.sun.istack.internal.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.lf.util.CollectionFactory.newHashMap;


public class Attributes {
	
	private Map<Class, Attribute> data = newHashMap();
	
	public Attributes createSuccessor() {
		Attributes result = new Attributes();
		Set<Entry<Class, Attribute>> set = data.entrySet();
		for (Entry<Class, Attribute> cur : set) {
			result.addAttribute(cur.getValue().createChild());
		}
		return result;
	}
	
	public <T extends Attribute> void addAttribute(T newAttribute) {
		if (data.containsKey(newAttribute.getClass()))
            throw new IllegalStateException(
                    "Attribute of class " + newAttribute.getClass() + " already present");
		data.put(newAttribute.getClass(), newAttribute);
	}
	
	@Nullable
	public <T extends Attribute> T getValue(Class<T> attributeClass){
		if (data.containsKey(attributeClass)) { 
			return (T) data.get(attributeClass);
		}
		return null;
	}
}
