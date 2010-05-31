package org.lf.ui.components.plugins.scrollablelog.extension.builtin;


import org.lf.plugins.tree.Bookmarks;

import javax.swing.*;

class BookmarksComboBoxModel extends AbstractListModel implements MutableComboBoxModel {
    private Bookmarks bookmarks;
    private Object selectedElement;

    public BookmarksComboBoxModel(Bookmarks bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public void addElement(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertElementAt(Object value, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeElement(Object index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeElementAt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSelectedItem() {
        return selectedElement;
    }

    @Override
    public void setSelectedItem(Object value) {
        selectedElement = value;
        fireContentsChanged(this, 0, getSize());
    }


    @Override
    public Object getElementAt(int index) {
        if (bookmarks.getSize() <= index || index < 0)
            throw new IndexOutOfBoundsException(
                    "" + index + " is not in bounds [0," + bookmarks.getSize() + ")");
        return bookmarks.getNames().get(index);
    }

    @Override
    public int getSize() {
        return bookmarks.getSize();
    }

    public void update() {
        fireContentsChanged(this, 0, getSize());
    }
}
