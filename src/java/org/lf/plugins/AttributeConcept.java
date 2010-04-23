package org.lf.plugins;

import org.lf.logs.Log;

import java.util.Collection;

public interface AttributeConcept<C extends AttributeConcept<C, I>, I extends AttributeInstance<C, I>> {
    I join(Collection<I> parents, Log attributeOwner);
}