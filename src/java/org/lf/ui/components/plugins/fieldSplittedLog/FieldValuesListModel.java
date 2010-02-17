package org.lf.ui.components.plugins.fieldSplittedLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;

import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.services.LogAndField;

import com.sun.istack.internal.Nullable;

class FieldValuesListModel extends AbstractListModel {
	ArrayList<String> arrayData =  new ArrayList<String>();
	private HashMap<String, JPanel> mapData = new HashMap<String, JPanel>();	
	private Position otherPosition;
	
	@Nullable
	public Position getOtherPosition() {
		return otherPosition;
	}
	public void fillModel(final LogAndField logAndField) {
		new Thread() {
			public void run(){
				try {
					Position cur = logAndField.log.first();
					Position prev = new Position() {};
					for (int i = 0; i < 1000; ++i) {
						if (cur.equals(prev)) break;
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
		}.start();
		
	}
	
	public void setView(String fieldValue, JPanel view) {
		if (!mapData.containsKey(fieldValue)) return;
		mapData.put(fieldValue, view);
	}

	public void addFieldValue(String fieldValue) {
		if (mapData.containsKey(fieldValue)) return;
		mapData.put(fieldValue, null);
		synchronized (arrayData) {
			arrayData.add(fieldValue);
		}
		fireIntervalAdded(this, arrayData.size() - 1, arrayData.size() - 1);
	}

	@Nullable
	public JPanel getView(String fieldValue) {
		//TODO check that there is no any field with value "other"
		return mapData.get(fieldValue);
	}
	
	@Nullable	
	public JPanel getView(int index) {
		if (index >= arrayData.size()) return null; 
		return getView(arrayData.get(index));
	}
	
	@Override
	public Object getElementAt(int arg0) {
		synchronized (arrayData) {
			return arrayData.get(arg0);			
		}
	}

	@Override
	public int getSize() {
		synchronized (arrayData) {
			return arrayData.size();
		}
	}

}
