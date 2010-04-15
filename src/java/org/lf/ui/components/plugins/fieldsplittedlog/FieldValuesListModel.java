package org.lf.ui.components.plugins.fieldsplittedlog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;

import org.lf.logs.Log;
import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.newHashMap;

class FieldValuesListModel extends AbstractListModel {
    private final List<String> values = newList();
    private final Map<String, Log> value2log = newHashMap();
    private Position endScanPosition;
    
    @Nullable
    public Position getEndScanPosition() {
        return endScanPosition;
    }

    String[] getValues() {
        return values.toArray(new String[0]);
    }
    
    void addStringValue(String String) {
//        if (value2panel.containsKey(String)) return;
//        value2panel.put(String, null);
//        values.add(String);
    }
    
    void setMaxReadedPosition(Position pos) {
        endScanPosition = pos;
    }

    void setView(String String, JPanel view) {
//        if (!value2panel.containsKey(String)) return;
//        value2panel.put(String, view);
    }

    @Nullable
    JPanel getView(String String) {
//        TODO check that there is no any String with value "other"
//        return value2panel.get(String);
        return null;
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
