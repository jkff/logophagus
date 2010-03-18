package org.lf.ui.components.plugins.fieldsplittedlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;

import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;

class FieldValuesListModel extends AbstractListModel {
    private final List<String> values = newList();
    private final Map<String, JPanel> value2panel = new HashMap<String, JPanel>();
    private Position otherPosition;

    @Nullable
    public Position getOtherPosition() {
        return otherPosition;
    }

    List<String> getValues() {
        return values.subList(0, values.size() -1);
    }
    
    void addFieldValue(String fieldValue) {
        if (value2panel.containsKey(fieldValue)) return;
        value2panel.put(fieldValue, null);
        values.add(fieldValue);
        // TODO Is this call necessary? I think not.
        fireIntervalAdded(this, values.size() - 1, values.size() - 1);
    }
    
    void setMaxReadedPosition(Position pos) {
        otherPosition = pos;
    }

    void setView(String fieldValue, JPanel view) {
        if (!value2panel.containsKey(fieldValue)) return;
        value2panel.put(fieldValue, view);
    }

    @Nullable
    JPanel getView(String fieldValue) {
        //TODO check that there is no any field with value "other"
        return value2panel.get(fieldValue);
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
