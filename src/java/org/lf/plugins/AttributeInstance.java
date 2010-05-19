package org.lf.plugins;

import org.lf.logs.Log;


public interface AttributeInstance<C extends AttributeConcept<C, I>, I extends AttributeInstance<C, I>> {
    C getConcept();

    /**
     * Create an instance of the same attribute whose Log
     * equals to attributeOwner.
     */
    public I createChild(Log attributeOwner);
}

