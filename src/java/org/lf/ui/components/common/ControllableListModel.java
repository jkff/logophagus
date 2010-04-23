package org.lf.ui.components.common;

import javax.swing.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newLinkedList;

public class ControllableListModel<T> extends AbstractListModel {
    private List<T> items = newLinkedList();

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int i) {
        if (i >= items.size()) return null;
        return items.get(i);
    }

    public void add(T newElement) {
        items.add(newElement);
        fireIntervalAdded(this, items.size() - 1, items.size() - 1);
    }

    public void remove(int index) {
        items.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void moveUp(int index) {
        if (index == 0 || index >= items.size()) return;
        items.add(index - 1, items.remove(index));
        fireContentsChanged(this, 0, items.size() - 1);
    }

    public void moveDown(int index) {
        if (index >= items.size() - 1) return;
        items.add(index + 1, items.remove(index));
        fireContentsChanged(this, 0, items.size() - 1);
    }

}
