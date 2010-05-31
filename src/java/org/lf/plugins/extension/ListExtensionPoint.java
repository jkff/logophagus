package org.lf.plugins.extension;

import org.lf.util.Removable;

import java.util.Collections;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class ListExtensionPoint<T> implements ExtensionPoint<T> {
    private List<T> items = newList();

    @Override
    public Removable addExtension(final T extension) {
        items.add(extension);
        return new Removable() {
            @Override
            public void remove() {
                items.remove(extension);
            }
        };
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }
}
