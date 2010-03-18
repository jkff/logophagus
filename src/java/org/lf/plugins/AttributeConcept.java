package org.lf.plugins;

public interface AttributeConcept<T extends AttributeConcept<T>> {
    AttributeInstance<T> join(AttributeInstance<T>[] children);
}