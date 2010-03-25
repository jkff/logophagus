package org.lf.ui.components.plugins.fieldsplittedlog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.lf.logs.Field;
import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;

class FieldValuesListModel extends AbstractListModel {
    private final List<Field> values = newList();
    private final Map<Field, JPanel> value2panel = new HashMap<Field, JPanel>();
    private Position otherPosition;

    @Nullable
    public Position getOtherPosition() {
        return otherPosition;
    }

    List<Field> getValues() {
        return values.subList(0, values.size() -1);
    }
    
    void addFieldValue(Field field) {
        if (value2panel.containsKey(field)) return;
        value2panel.put(field, null);
        values.add(field);
    }
    
    void setMaxReadedPosition(Position pos) {
        otherPosition = pos;
    }

    void setView(Field field, JPanel view) {
        if (!value2panel.containsKey(field)) return;
        value2panel.put(field, view);
    }

    @Nullable
    JPanel getView(Field field) {
        //TODO check that there is no any field with value "other"
        return value2panel.get(field);
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
