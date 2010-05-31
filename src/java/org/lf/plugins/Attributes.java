package org.lf.plugins;

import org.jetbrains.annotations.Nullable;
import org.lf.logs.Log;

import java.util.*;
import java.util.Map.Entry;

import static org.lf.util.CollectionFactory.newHashMap;


public class Attributes {
    private Map<Class, AttributeInstance<?, ?>> data = newHashMap();

    public Attributes createSuccessor(Log attributesOwner) {
        Attributes result = new Attributes();
        Set<Entry<Class, AttributeInstance<?, ?>>> set = data.entrySet();
        for (Entry<Class, AttributeInstance<?, ?>> cur : set) {
            result.addAttribute(cur.getValue().createChild(attributesOwner));
        }
        return result;
    }

    public <T extends AttributeInstance<?, ?>> void addAttribute(T newAttribute) {
        if (data.containsKey(newAttribute.getClass()))
            throw new IllegalStateException(
                    "Attribute of class " + newAttribute.getClass() + " already present");
        data.put(newAttribute.getClass(), newAttribute);
    }

    @Nullable
    public <T extends AttributeInstance<?, ?>> T getValue(Class<T> attributeClass) {
        if (data.containsKey(attributeClass)) {
            return (T) data.get(attributeClass);
        }
        return null;
    }

    public static Attributes join(Attributes[] others, Log attributeOwner) {
        Attributes result = new Attributes();

        Map<Class, Collection<AttributeInstance<?, ?>>> othersAttrMap = new HashMap<Class, Collection<AttributeInstance<?, ?>>>();

        for (Attributes attributes : others) {

            Set<Class> keys = attributes.data.keySet();
            for (Class clazz : keys) {
                if (!othersAttrMap.containsKey(clazz))
                    othersAttrMap.put(clazz, new LinkedList<AttributeInstance<?, ?>>());
            }

            Collection<AttributeInstance<?, ?>> attrCollection = attributes.data.values();
            for (AttributeInstance<?, ?> attributeInstance : attrCollection) {
                othersAttrMap.get(attributeInstance.getClass()).add(attributeInstance);
            }
        }

        for (Entry<Class, Collection<AttributeInstance<?, ?>>> entry : othersAttrMap.entrySet()) {
            AttributeInstance instance = entry.getValue().iterator().next();
            AttributeInstance joinedAttribute = instance.getConcept().join(entry.getValue(), attributeOwner);
            result.addAttribute(joinedAttribute);
        }

        return result;
    }
}
