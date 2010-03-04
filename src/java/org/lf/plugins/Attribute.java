package org.lf.plugins;


public interface Attribute<T extends Attribute<T>> {
    public Attribute<T> getParent();
    /**
     * Create an instance of the same attribute whose parent
     * equals to 'this'.
     */
	public Attribute<T> createChild();
	
}
