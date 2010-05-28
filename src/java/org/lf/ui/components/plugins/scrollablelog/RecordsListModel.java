package org.lf.ui.components.plugins.scrollablelog;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

class RecordsListModel extends AbstractListModel implements Observer {
    private int lastUpdateSize = 0;

    private ScrollableLogModel underlyingModel;

    public RecordsListModel(ScrollableLogModel underlyingModel) {
        this.underlyingModel = underlyingModel;
        this.underlyingModel.addObserver(this);
    }

    @Override
    public Object getElementAt(int index) {
        return underlyingModel.getRecord(index);
    }

    @Override
    public int getSize() {
        return underlyingModel.getShownRecordCount();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (underlyingModel.getShownRecordCount() != lastUpdateSize)
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireContentsChanged(RecordsListModel.this, 0, underlyingModel.getShownRecordCount());
                }
            });
        lastUpdateSize = underlyingModel.getShownRecordCount();
    }
}