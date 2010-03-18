package org.lf.plugins;


public interface AttributeInstance<T extends AttributeConcept<T>> {
    AttributeConcept<T> getConcept();
    
    public AttributeInstance<T> getParent();
    /**
     * Create an instance of the same attribute whose parent
     * equals to 'this'.
     */
    public AttributeInstance<T> createChild();
}

