package org.lf.plugins.extension;

public interface ExtensionPoint<T extends Extension> {
    void addExtension(T extension);
}
