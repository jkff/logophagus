package org.lf.logs;

public abstract class Field {

    public enum Type{
        TEXT,
        NUMBER,
        DATE,
        TIME
    }
    public abstract Type getType();
    public abstract String getName();
    public abstract int getIndexInRecord();
    public abstract Object getValue();

    @Override
    public String toString() {
        return (String)getValue();
    }
    
    @Override
    public int hashCode() {
    	return getValue().hashCode();
    	
    }
    
    @Override
    public boolean equals(Object other) {
        return  other != null								&&
        		other.getClass().equals(Field.class)        &&
                ((Field)other).getName().equals(getName())  &&
                ((Field)other).getType().equals(getType())  &&
                ((Field)other).getValue().equals(getValue());
    }


}
