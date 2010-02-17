package org.lf.ui.components.plugins.scrollableLogTable;



import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import org.lf.parser.Position;
import org.lf.services.Bookmarks;
import org.lf.util.Pair;

class BookmarksComboBoxModel extends AbstractListModel implements MutableComboBoxModel {
	private Bookmarks bookmarks;
	private Object selectedElement;
	
	public BookmarksComboBoxModel(Bookmarks bookmarks) {
		this.bookmarks = bookmarks;
	}
	
	@Override
	public void addElement(Object arg0) {
		Pair<String,Position> data = (Pair<String, Position>) arg0;
		bookmarks.addBookmark(data.first, data.second);
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void insertElementAt(Object arg0, int arg1) {
		addElement(arg0);
	}

	@Override
	public void removeElement(Object arg0) { }

	@Override
	public void removeElementAt(int arg0) {	}

	@Override
	public Object getSelectedItem() {
		return selectedElement;
	}

	@Override
	public void setSelectedItem(Object arg0) {
		selectedElement = arg0;
		fireContentsChanged(this, 0, getSize());
	}


	@Override
	public Object getElementAt(int arg0) {
		if (bookmarks.getSize() <= arg0 || arg0 < 0) return null;
		return bookmarks.getNames().get(arg0);
	}

	@Override
	public int getSize() {
		return bookmarks.getSize();
	}
	
	public void update() {
		fireContentsChanged(this,0, getSize());
	}
}
