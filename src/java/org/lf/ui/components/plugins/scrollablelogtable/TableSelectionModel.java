package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListSelectionModel;
import javax.swing.SwingUtilities;

public class TableSelectionModel extends DefaultListSelectionModel implements Observer{
	private ScrollableLogViewModel model;

	public TableSelectionModel(ScrollableLogViewModel model) {
		super();
		this.model = model;
		setSelectionMode(SINGLE_SELECTION);
		model.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Update selection");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				clearSelection();
				setSelectionInterval(getMaxSelectionIndex(), getMaxSelectionIndex());
				fireValueChanged(true);
			}
		});
	}

}
