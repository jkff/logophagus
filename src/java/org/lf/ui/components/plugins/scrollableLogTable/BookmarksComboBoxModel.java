package org.lf.ui.components.plugins.scrollableLogTable;



import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import org.lf.parser.Position;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.util.Pair;

class BookmarksComboBoxModel extends AbstractListModel implements MutableComboBoxModel {
	private Bookmarks bookmarks;
	private Object selectedElement;
	
	public BookmarksComboBoxModel(Bookmarks bookmarks) {
		this.bookmarks = bookmarks;
	}
	
	@Override
	public void addElement(Object value) {
		Pair<String,Position> data = (Pair<String, Position>) value;
		bookmarks.addBookmark(data.first, data.second);
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void insertElementAt(Object value, int index) {
		addElement(value);
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
                    "" + index + " is not in bounds [0,"+bookmarks.getSize()+")");
		return bookmarks.getNames().get(index);
	}

	@Override
	public int getSize() {
		return bookmarks.getSize();
	}
	
	public void update() {
		fireContentsChanged(this,0, getSize());
	}
}
