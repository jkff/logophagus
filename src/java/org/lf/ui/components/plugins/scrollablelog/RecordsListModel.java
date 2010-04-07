package org.lf.ui.components.plugins.scrollablelog;

import javax.swing.AbstractListModel;

class RecordsListModel extends AbstractListModel{
    private ScrollableLogModel underlyingModel;

    public RecordsListModel(ScrollableLogModel underlyingModel) {
        this.underlyingModel = underlyingModel;
    }

    @Override
	public Object getElementAt(int index) {
		return underlyingModel.getRecord(index);
	}

	@Override
	public int getSize() {
		return underlyingModel.getRecordCount();
	}

}