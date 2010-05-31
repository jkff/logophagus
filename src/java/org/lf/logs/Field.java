package org.lf.logs;


public final class Field {
    public final String name;
    public final Type type;

    public Field(String name) {
        this(name, Type.TEXT);
    }

    public Field(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public enum Type {
        DATE,
        TIME,
        TEXT,
        NUMBER,
    }


    @Override
    public boolean equals(Object o) {
        return o != null &&
                o.getClass() == this.getClass() &&
                ((Field) o).name.equals(name) &&
                ((Field) o).type.equals(type);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
