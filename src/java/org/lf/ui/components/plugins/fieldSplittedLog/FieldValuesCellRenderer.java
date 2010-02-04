package org.lf.ui.components.plugins.fieldSplittedLog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

class FieldValuesCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1,
			int arg2, boolean arg3, boolean arg4) {
		JLabel label = (JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
		FieldValuesListModel model = (FieldValuesListModel)arg0.getModel();
		if (model.getView(arg2) == null) 
			return label; 
		label.setForeground(Color.BLUE);
		return label;
	}

}
