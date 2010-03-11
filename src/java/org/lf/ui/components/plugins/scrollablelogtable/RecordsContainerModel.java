package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.LinkedList;
import java.util.List;

import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Pair;

class RecordsContainerModel {
	private final List<Pair<Record,Position>> recAndPos;
	private final int maxSize;
	
	RecordsContainerModel(int maxSize) {
		this.recAndPos = new LinkedList<Pair<Record,Position>>();
		this.maxSize = maxSize;
	}
	
	void pushBegin(Pair<Record, Position> pair) { 
		if (recAndPos.size() == maxSize) 
			recAndPos.remove(maxSize-1);
		recAndPos.add(0, pair);
	}
	
	void pushEnd(Pair<Record, Position> pair) {
		if (recAndPos.size() == maxSize) 
			recAndPos.remove(0);
		recAndPos.add(pair);		
	}
	
	Record getRecord(int index) {
		return recAndPos.get(index).first;
	}

	Position getPosition(int index) {
		return recAndPos.get(index).second;
	}

	void clear() {
		recAndPos.clear();
	}

	int maxSize() {
		return maxSize;
	}

	int size() {
		return recAndPos.size();
	}

}
