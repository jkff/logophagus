package org.lf.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sun.istack.internal.Nullable;


/**
 * User: jkff
 * Date: Jan 26, 2010
 * Time: 3:45:26 PM
 */
public class Attributes {
	
	private Map<Class, Attribute> data = new HashMap< Class, Attribute>();
	
	public Attributes createSuccessor() {
		Attributes result = new Attributes();
		Set<Entry<Class, Attribute>> set = data.entrySet();
		for (Entry<Class, Attribute> cur : set) {
			result.addAttribute(cur.getValue().createChild());
		}
		return result;
	}
	
	public <T extends Attribute> void addAttribute(T newAttribute) {
		if (data.containsKey(newAttribute.getClass())) return;
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
