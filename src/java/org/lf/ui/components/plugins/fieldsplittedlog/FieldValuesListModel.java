package org.lf.ui.components.plugins.fieldsplittedlog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;

import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.plugins.analysis.splitbyfield.LogAndField;

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

    public void fillModel(final LogAndField logAndField) {
        try {
            Position cur = logAndField.log.first();
            Position prev = null;
            for (int i = 0; i < 1000; ++i) {
                if (prev != null && cur.equals(prev)) break;
                Record rec = logAndField.log.readRecord(cur);
                FieldValuesListModel.this.addFieldValue(rec.get(logAndField.field));
                prev = cur;
                cur = logAndField.log.next(cur);
            }
            otherPosition = cur;
            FieldValuesListModel.this.addFieldValue("Other");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void addFieldValue(String fieldValue) {
        if (value2panel.containsKey(fieldValue)) return;
        value2panel.put(fieldValue, null);
        values.add(fieldValue);
        // TODO Is this call necessary? I think not.
        fireIntervalAdded(this, values.size() - 1, values.size() - 1);
    }
    

    public synchronized void setView(String fieldValue, JPanel view) {
        if (!value2panel.containsKey(fieldValue)) return;
        value2panel.put(fieldValue, view);
    }

    @Nullable
    public synchronized JPanel getView(String fieldValue) {
        //TODO check that there is no any field with value "other"
        return value2panel.get(fieldValue);
    }

    @Nullable
    public synchronized JPanel getView(int index) {
        if (index >= values.size()) return null;
        return getView(values.get(index));
    }

    @Override
    public synchronized Object getElementAt(int i) {
        return values.get(i);
    }

    @Override
    public synchronized int getSize() {
        return values.size();
    }
}
