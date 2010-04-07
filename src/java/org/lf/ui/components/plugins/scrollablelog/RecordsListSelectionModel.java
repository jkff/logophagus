package org.lf.ui.components.plugins.scrollablelog;


import javax.swing.DefaultListSelectionModel;

public class RecordsListSelectionModel extends DefaultListSelectionModel {
    private int cachedIndex;

    public RecordsListSelectionModel() {
        super();
        cachedIndex = 0;
        setSelectionMode(SINGLE_SELECTION);
    }

    @Override
    public int getAnchorSelectionIndex() {
        int index = super.getAnchorSelectionIndex();
        if (index == -1) {
            setAnchorSelectionIndex(cachedIndex);
            return cachedIndex;
        }
        cachedIndex = index;
        return index;
    }

    @Override
    public int getLeadSelectionIndex() {
        int index = super.getLeadSelectionIndex();
        if (index == -1) {
            setLeadSelectionIndex(cachedIndex);
            return cachedIndex;
        }
        cachedIndex = index;
        return index;
    }

    @Override
    public int getMaxSelectionIndex() {
        int index = super.getMaxSelectionIndex();
        if (index == -1) {
            setSelectionInterval(cachedIndex, cachedIndex);
            return cachedIndex;
        }
        cachedIndex = index;
        return index;
    }

    @Override
    public int getMinSelectionIndex() {
        int index = super.getMinSelectionIndex();
        if (index == -1) {
            setSelectionInterval(cachedIndex, cachedIndex);
            return cachedIndex;
        }
        cachedIndex = index;
        return index;
    }


}
