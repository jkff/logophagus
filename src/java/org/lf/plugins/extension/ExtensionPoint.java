package org.lf.plugins.extension;

import org.lf.util.Removable;

public interface ExtensionPoint<T> {
    public Removable addExtension(T extension);
}
