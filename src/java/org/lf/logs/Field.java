package org.lf.logs;

public abstract class Field {

    public enum Type{
        TEXT,
        NUMBER,
        DATE,
        TIME
    }

    public abstract String getName();
    public abstract Type getType();

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object other) {
        return  other.getClass().equals(Field.class)         &&
                ((Field)other).getName().equals(getName())  &&
                ((Field)other).getType().equals(getType())      ;
    }


}
