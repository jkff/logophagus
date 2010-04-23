package org.lf.ui.components.plugins.fieldsplittedlog;

import org.lf.logs.Cell;

import javax.swing.*;
import java.awt.*;

class FieldValuesCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, ((Cell) value).getValue(), index, isSelected, cellHasFocus);
        FieldValuesListModel model = (FieldValuesListModel) list.getModel();
        if (model.getView(index) == null)
            return label;
        label.setForeground(Color.BLUE);
        return label;
    }

}
