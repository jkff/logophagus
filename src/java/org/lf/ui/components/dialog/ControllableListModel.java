package org.lf.ui.components.dialog;

import javax.swing.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newLinkedList;

public class ControllableListModel<T> extends AbstractListModel{
    private List<T> recordFormats = newLinkedList();

    @Override
    public int getSize() {
        return recordFormats.size();
    }

    @Override
    public T getElementAt(int i) {
        if (i >= recordFormats.size()) return null;
        return recordFormats.get(i);
    }

    public void add(T newElement) {
        recordFormats.add(newElement);
        fireIntervalAdded(this, recordFormats.size()-1, recordFormats.size()-1);
    }

    public void remove(int index) {
        recordFormats.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void moveUp(int index) {
        if (index == 0 || index >= recordFormats.size() ) return;
        recordFormats.add(index - 1 , recordFormats.remove(index));
        fireContentsChanged(this, 0, recordFormats.size()-1);
    }

    public void moveDown(int index) {
        if ( index >= recordFormats.size() - 1 ) return;
        recordFormats.add(index + 1 , recordFormats.remove(index));
        fireContentsChanged(this, 0, recordFormats.size()-1);
    }

}
