package org.lf.ui.components.plugins.fieldsplittedlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;

import org.lf.logs.Cell;
import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;

class FieldValuesListModel extends AbstractListModel {
    private final List<Cell> values = newList();
    private final Map<Cell, JPanel> value2panel = new HashMap<Cell, JPanel>();
    private Position otherPosition;

    @Nullable
    public Position getOtherPosition() {
        return otherPosition;
    }

    List<Cell> getValues() {
        return values.subList(0, values.size() -1);
    }
    
    void addFieldValue(Cell cell) {
        if (value2panel.containsKey(cell)) return;
        value2panel.put(cell, null);
        values.add(cell);
    }
    
    void setMaxReadedPosition(Position pos) {
        otherPosition = pos;
    }

    void setView(Cell cell, JPanel view) {
        if (!value2panel.containsKey(cell)) return;
        value2panel.put(cell, view);
    }

    @Nullable
    JPanel getView(Cell cell) {
        //TODO check that there is no any field with value "other"
        return value2panel.get(cell);
    }

    @Nullable
    JPanel getView(int index) {
        if (index >= values.size()) return null;
        return getView(values.get(index));
    }

    @Override
    public Object getElementAt(int i) {
        return values.get(i);
    }

    @Override
    public int getSize() {
        return values.size();
    }
}
