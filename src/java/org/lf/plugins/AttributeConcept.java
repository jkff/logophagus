package org.lf.plugins;

import java.util.Collection;
import java.util.List;

public interface AttributeConcept<C extends AttributeConcept<C,I>, I extends AttributeInstance<C,I>> {
    I join(Collection<I> parents);
}