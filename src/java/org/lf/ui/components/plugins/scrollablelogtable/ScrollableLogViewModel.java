package org.lf.ui.components.plugins.scrollablelogtable;

import static org.lf.util.CollectionFactory.pair;

import java.io.IOException;
import java.util.Observable;


import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Pair;

import com.sun.istack.internal.Nullable;

public class ScrollableLogViewModel extends Observable {
	// TODO Read "Java concurrency in practice" and insert proper synchronization
	private final RecordsContainerModel container;
	private final int regionSize;
	private final Log log;
	private Thread navigatorThread;
	private Position logBeginPos;
	private Position logEndPos;
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
			try {
				if (logBeginPos == null)	logBeginPos = log.first();
				if (logBeginPos == null)  	return;
				if (logEndPos == null)  	logEndPos = log.last();
				if (fromWhere == null)		fromWhere = logBeginPos;

				if (recordsToRead == regionSize)	clear();

				int reflectionCount = 0;
				Position tempPos = fromWhere;

				// TODO Make this comprehensible to everyone reading the code
				for (int i = 0; i < recordsToRead; ++i) {
					if (i == 0 && readFromWhere) 
						if (directionForward) 
							pushBegin(pair(log.readRecord(tempPos), tempPos));
						else 
							pushEnd(pair(log.readRecord(tempPos), tempPos));

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
					if (tempPos == null ||  (i == recordsToRead - 1 && readFromWhere))
						break;
					if (directionForward) 
						pushEnd(pair(log.readRecord(tempPos), tempPos));
					else 
						pushBegin(pair(log.readRecord(tempPos), tempPos));
				}

				setReadingDone(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	public ScrollableLogViewModel(Log log, int regionSize) {
		this.log = log;
		this.regionSize = regionSize;
		this.readingDone = true;
		this.container = new RecordsContainerModel(regionSize);
	}

	@Nullable
	public Record getRecord(int index) {
		if (index >= container.size()) return null;
		return container.getRecord(index);
	}

	synchronized void setReadingDone(boolean isDone) {
		readingDone = isDone;
	}

	synchronized public void start() {
		if (!readingDone || isAtBegin()) return;
		readingDone = false;
		navigatorThread = new Navigator();
		navigatorThread.start();
	}

	synchronized public void end() {
		if (!readingDone || isAtEnd()) return;
		readingDone = false;
		navigatorThread = new Navigator(false, logEndPos, regionSize, true);
		navigatorThread.start();
	}

	synchronized public void next() {
		if (!readingDone || isAtEnd()) return;
		readingDone = false;
		navigatorThread = new Navigator(true, getPosition(getRecordCount() - 1), regionSize, false);
		navigatorThread.start();
	}

	synchronized public void prev() {
		if (!readingDone || isAtBegin()) return;
		readingDone = false;
		navigatorThread = new Navigator(false, getPosition(0), regionSize, false);
		navigatorThread.start();
	}

	synchronized public void shiftUp() {
		if (!readingDone || isAtBegin()) return;
		readingDone = false;
		navigatorThread = new Navigator(false, getPosition(0), 1, false);
		navigatorThread.start();
	}

	synchronized public void shiftDown() {
		if (!readingDone || isAtEnd()) return;
		readingDone = false;
		navigatorThread = new Navigator(true, getPosition(getRecordCount() - 1), 1, false);
		navigatorThread.start();
	}

	synchronized public void shiftTo(Position pos) {
		if (!readingDone || pos.equals(getPosition(0))) return;
		readingDone = false;
		navigatorThread = new Navigator(true, pos, regionSize, true);
		navigatorThread.start();
	}

	@Nullable
	synchronized public Position getPosition(int index) {
		if (index >= container.size()) return null;
		return container.getPosition(index);
	}

	synchronized public int getRecordCount() {
		return container.size();
	}

	synchronized public int getRegionSize() {
		return regionSize;
	}

	synchronized public boolean isReadingDone() {
		return readingDone;
	}

	synchronized public boolean isAtBegin() {
		if (!readingDone) return false;
		if (container.size() == 0) return false;
		if (container.getPosition(0).equals(logBeginPos) ) return true;
		return false;
	}

	synchronized public boolean isAtEnd() {
		if (!readingDone) return false;
		if (container.size() == 0) return false;
		if (container.getPosition(container.size() - 1).equals(logEndPos) ) return true;
		return false;
	}

	int getRecordSize() {
		return log.getFields().length;
	}

	synchronized private void pushBegin(Pair<Record, Position> pair) {
		if (pair.first.size() > getRecordSize()) {
			setChanged();
			notifyObservers("CHANGE_RECORD_SIZE");
		}
		container.pushBegin(pair);
		setChanged();
		notifyObservers("ADD_BEGIN");
	}

	synchronized private void pushEnd(Pair<Record, Position> pair) {
		if (pair.first.size() > getRecordSize()) {
			setChanged();
			notifyObservers("CHANGE_RECORD_SIZE");
		}
		container.pushEnd(pair);
		setChanged();
		notifyObservers("ADD_END");
	}


	synchronized private void clear() {
		container.clear();			
		setChanged();
		notifyObservers("CLEAR");
	}



}
