package org.lf.logs;

public abstract class Cell {
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
        return getValue().toString();
    }
    
    @Override
    public int hashCode() {
    	return getValue().hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return  other != null								&&
        		other.getClass().equals(Cell.class)        &&
                ((Cell)other).getName().equals(getName())  &&
                ((Cell)other).getType().equals(getType())  &&
                ((Cell)other).getValue().equals(getValue());
    }


}
