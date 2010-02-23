package org.lf.ui.components.plugins.fieldsplittedlog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

class FieldValuesCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		FieldValuesListModel model = (FieldValuesListModel)list.getModel();
		if (model.getView(index) == null) 
			return label; 
		label.setForeground(Color.BLUE);
		return label;
	}

}
