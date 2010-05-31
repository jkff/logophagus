package org.lf.plugins.extension;

public class ExtensionPointID<T> {
    public static <T> ExtensionPointID<T> create() {
        return new ExtensionPointID<T>();
    }
}