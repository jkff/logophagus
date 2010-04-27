package org.lf.plugins.extension;

public abstract class ExtensionID {
    public abstract String getName();


    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this.getClass().isAssignableFrom(other.getClass()) &&
                ((ExtensionID) other).getName().equals(this.getName());
    }
}
