package org.lf.plugins;

import java.util.Collection;

import org.lf.logs.Log;

public interface AttributeConcept<C extends AttributeConcept<C,I>, I extends AttributeInstance<C,I>> {
    I join(Collection<I> parents, Log attributeOwner);
}