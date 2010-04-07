package org.lf.ui.components.plugins.scrollablelog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.lf.logs.Record;
import org.lf.plugins.analysis.highlight.Highlighter;

public class RecordView extends JPanel implements ListCellRenderer {
	private final ScrollableLogModel model;
	private final Highlighter highlighter;
	private JTextArea[] jCells = new JTextArea[10];

	public RecordView(ScrollableLogModel model, Highlighter highlighter) {
		this.model = model;
		this.highlighter = highlighter;
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));

		for (int i = 0; i < jCells.length; ++i) {
			jCells[i] = new JTextArea();
//			jCells[i].setLineWrap(false);
			jCells[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
			add(jCells[i]);
		}
	}

	@Override
	public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) 
	{
		if (value == null || value.getClass().isAssignableFrom(Record.class)) return null;
		
		Record record = (Record)value;
		String[] cellValues = record.getCellValues();
		int maxHeight  = 0;
		for (int i = 0; i < cellValues.length; ++i) {
			jCells[i].setText(cellValues[i]);
			if (jCells[i].getPreferredSize().height > maxHeight) maxHeight = jCells[i].getPreferredSize().height;
		}

		for (int i = 0; i < cellValues.length; ++i) {
			setVisible(true);
			Dimension d = jCells[i].getPreferredSize();
			d.height = maxHeight;
			jCells[i].setPreferredSize(d);
//			jCells[i].setVisible(true);
		}
		
		for (int i = cellValues.length; i < jCells.length ; i++) {
			jCells[i].setVisible(false);
		}
		
		if (isSelected) { 
			this.setBackground(UIManager.getColor("List.selectionBackground"));
		} else {
			Color color;
			if (highlighter != null)   
				color = highlighter.getHighlightColor(record);
			else
				color = UIManager.getColor("List.background");
			this.setBackground(color);
		}
		
		this.setVisible(true);
		
		return this;
	}

}
