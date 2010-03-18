package org.lf.plugins;


public interface AttributeInstance<C extends AttributeConcept<C,I>, I extends AttributeInstance<C,I>> {
    C getConcept();
    
    /**
     * Create an instance of the same attribute whose parent
     * equals to 'this'.
     */
    public I createChild();
}

