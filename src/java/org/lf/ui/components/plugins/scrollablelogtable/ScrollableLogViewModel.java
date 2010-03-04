package org.lf.ui.components.plugins.scrollablelogtable;

import static org.lf.util.CollectionFactory.pair;
import static org.lf.util.CollectionFactory.newLinkedList;

import java.io.IOException;
import java.util.List;
import java.util.Observable;


import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Pair;

import com.sun.istack.internal.Nullable;

public class ScrollableLogViewModel extends Observable {
    // TODO Read "Java concurrency in practice" and insert proper synchronization
	private final List<Pair<Record,Position>> recAndPos = newLinkedList();
	private final int regionSize;
	private final Log log;
	private Thread navigatorThread;
	private Position logBeginPos;
	private Position logEndPos;
	private int maxRecordSize = 0;
	private boolean readingDone; 

	private class Navigator extends Thread {
		private boolean directionForward;
		private Position fromWhere;
		private int recordsToRead;
		private boolean readFromWhere;
		
		public Navigator() {
			this.directionForward = true;
			this.fromWhere = logBeginPos;
			this.recordsToRead = regionSize;
			this.readFromWhere = true;
		}

		public Navigator(boolean directionForward, Position fromWhere, int recordsToRead, boolean readFromWhere) {
			this.directionForward = directionForward;
			this.fromWhere = fromWhere;
			this.recordsToRead = recordsToRead;
			this.readFromWhere = readFromWhere;
		}

		@Override
		public void run() {
			readingDone = false;
			try {
				if (logBeginPos == null)	logBeginPos = log.first();
				if (logBeginPos == null)  	return;
				if (logEndPos == null)  	logEndPos = log.last();
				if (fromWhere == null)		fromWhere = logBeginPos;
				
				if (recordsToRead == regionSize)	clear();
				else    							if (directionForward)	remove(0, recordsToRead);
													else 					remove(getRecordCount() - recordsToRead, getRecordCount());	
				
				int reflectionCount = 0;
				Position tempPos = fromWhere;

                // TODO Make this comprehensible to everyone reading the code
				for (int i = 0; i < recordsToRead; ++i) {
					if (i == 0 && readFromWhere)
						add(directionForward ? getRecordCount() : 0, pair(log.readRecord(tempPos), tempPos));

					if (tempPos.equals(logBeginPos)) {
						if (++reflectionCount == 2 ) break;
						if (!directionForward) {
							directionForward = !directionForward;
							tempPos = fromWhere;
						}
					} else if (tempPos.equals(logEndPos)) {
						if (++reflectionCount == 2 ) break;
						if (directionForward) {
							directionForward = !directionForward;
							tempPos = fromWhere;
						}						
					}
					
					tempPos = directionForward ? log.next(tempPos) : log.prev(tempPos);
					//TODO look why this can be
					if (tempPos == null)
						break;
					add(directionForward ? getRecordCount() : 0, pair(log.readRecord(tempPos), tempPos));
				}

				readingDone = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	public ScrollableLogViewModel(Log log, int regionSize) {
		this.log = log;
		this.regionSize = regionSize;
		this.readingDone = true;
	}

	@Nullable
	public Record getRecord(int index) {
		synchronized (recAndPos) {
			if (index >= recAndPos.size()) return null;
			return recAndPos.get(index).first;
		}
	}

	public void start() {
		if (!readingDone || isAtBegin()) return;
		navigatorThread = new Navigator();
		navigatorThread.start();
	}

	public void end() {
		if (!readingDone || isAtEnd()) return;
		navigatorThread = new Navigator(false, logEndPos, regionSize, true);
		navigatorThread.start();
	}

	public void next() {
		if (!readingDone || isAtEnd()) return;
		navigatorThread = new Navigator(true, getPosition(getRecordCount() - 1), regionSize, false);
		navigatorThread.start();
	}

	public void prev() {
		if (!readingDone || isAtBegin()) return;
		navigatorThread = new Navigator(false, getPosition(0), regionSize, false);
		navigatorThread.start();
	}

	public void shiftUp() {
		if (!readingDone || isAtBegin()) return;
		navigatorThread = new Navigator(false, getPosition(0), 1, false);
		navigatorThread.start();
	}
	
	public void shiftDown() {
		if (!readingDone || isAtEnd()) return;
		navigatorThread = new Navigator(true, getPosition(getRecordCount() - 1), 1, false);
		navigatorThread.start();
	}

	public void shiftTo(Position pos) {
		if (!readingDone || pos.equals(getPosition(0))) return;
		navigatorThread = new Navigator(true, pos, regionSize, true);
		navigatorThread.start();
	}
	
	@Nullable
	public Position getPosition(int index) {
		synchronized (recAndPos) {
			if (recAndPos.size() <= index) return null;
			return recAndPos.get(index).second;			
		}
	}

	public int getRecordCount() {
		synchronized (recAndPos) {
			return recAndPos.size();
		}
	}

	public int getRegionSize() {
		return regionSize;
	}

	public boolean isReadingDone() {
		return readingDone;
	}

	public boolean isAtBegin() {
		if (!readingDone) return false;
		synchronized (recAndPos) {
			if (recAndPos.size() == 0) return false;
			if (recAndPos.get(0).second.equals(logBeginPos) ) return true;
			return false;
		}
	}

	public boolean isAtEnd() {
		if (!readingDone) return false;
		synchronized (recAndPos) {
			if (recAndPos.size() == 0) return false;
			if (recAndPos.get(recAndPos.size() - 1).second.equals(logEndPos) ) return true;
			return false;
		}

	}

	int getMaxRecordSize() {
		return maxRecordSize;
	}
	
	private void add(int pos ,Pair<Record, Position> pair) {
		if (maxRecordSize < pair.first.size())
			maxRecordSize = pair.first.size();
		setChanged();
		notifyObservers("maxRecordSize");
		synchronized (recAndPos) {
			recAndPos.add(pos, pair);
		}
		setChanged();
		notifyObservers("add");
	}

	private void clear() {
		synchronized (recAndPos) {
			recAndPos.clear();			
		}
		setChanged();
		notifyObservers("clear");
	}

	private void remove(int index) {
		synchronized (recAndPos) {
			recAndPos.remove(index);			
		}
		setChanged();
		notifyObservers("remove");
	}

	private void remove(int start, int end) {
		synchronized (recAndPos) {
			for (int i = start; i < end; ++i ) {
				recAndPos.remove(i);
			}
		}
		setChanged();
		notifyObservers("remove");
	}


}
