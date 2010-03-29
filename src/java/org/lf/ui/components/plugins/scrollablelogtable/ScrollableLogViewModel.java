package org.lf.ui.components.plugins.scrollablelogtable;

import static org.lf.util.CollectionFactory.pair;

import java.io.IOException;
import java.util.Observable;


import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.util.Pair;

import com.sun.istack.internal.Nullable;

public class ScrollableLogViewModel extends Observable {
	// TODO Read "Java concurrency in practice" and insert proper synchronization
	private final CyclicBuffer<Pair<Record,Position>> recBuffer;
	private final int regionSize;
	private final Log log;
	private Thread navigatorThread;
	private Position logBeginPos;
	private Position logEndPos;
	private boolean readingDone;

	private abstract class Navigator extends Thread {
		//adds records to model. startPos record included
		final int addRecordsToModel(Position startPos ,int recordsCount, boolean isForward) throws IOException {
			Position temp = startPos;
			int counter = 1;
			for ( ; counter <= recordsCount; ++counter ) {
				if (isForward) {
					pushEnd(pair(log.readRecord(temp), temp));
					if (logEndPos.equals(temp))
						break;
					temp =  log.next(temp);	
				} else {
					pushBegin(pair(log.readRecord(temp), temp));
					if (logBeginPos.equals(temp))
						break;
					temp =  log.prev(temp);	
				}
			}
			return counter; 
		}
	}


	private class RelativePosNavigator extends Navigator {
		private final boolean isForward;
		private final Position fromWhere;
		private final int recordsToRead;

		public RelativePosNavigator(boolean isForward, int recordsToRead) {
			this.fromWhere = getPosition(isForward ? getRecordCount() - 1 : 0) ;
			this.isForward = isForward;
			this.recordsToRead = recordsToRead;
		}

		@Override
		public void run() {
			try {
				Position tempPos;
				if (isForward) {
					if (logEndPos.equals(fromWhere)) return;
					tempPos = log.next(fromWhere);
				} else {
					if (logBeginPos.equals(fromWhere)) return;
					tempPos = log.prev(fromWhere);
				}
				addRecordsToModel(tempPos, recordsToRead, isForward);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				setReadingDone(true);
			}
		}
	}

	private class AbsolutePosNavigator extends Navigator {
		private final boolean isForward;
		private Position fromWhere;
		//if this constructor is used then reading will be done from log start 
		public AbsolutePosNavigator() {
			this.isForward = true;
			this.fromWhere = null;
		}
		public AbsolutePosNavigator(Position fromWhere, boolean isForward) {
			this.fromWhere = fromWhere;
			this.isForward = isForward;
		}
		@Override
		public void run() {
			try {
				if (logBeginPos == null)    logBeginPos = log.first();
				if (logBeginPos == null)	return;
				if (logEndPos == null)		logEndPos = log.last();
				if (logEndPos == null)  	return;
				if (fromWhere == null)      fromWhere = logBeginPos;
				clear();

				int readedRecords = addRecordsToModel(fromWhere, regionSize, isForward);
				if (readedRecords != regionSize) { 
					//now we will read missing records moving in reverse direction
					Position tempPos;
					if (isForward) {
						if (logBeginPos.equals(fromWhere)) return;
						tempPos = log.prev(fromWhere);
					} else {
						if (logEndPos.equals(fromWhere)) return;
						tempPos = log.next(fromWhere);
					}
					addRecordsToModel(tempPos, regionSize - readedRecords, !isForward);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				setReadingDone(true);
			}
		}
	}


	public ScrollableLogViewModel(Log log, int regionSize) {
		this.log = log;
		this.regionSize = regionSize;
		this.readingDone = true;
		this.recBuffer = new CyclicBuffer<Pair<Record, Position>>(regionSize);
		this.start();
	}

	@Nullable
	synchronized public Record getRecord(int index) {
		if (index >= recBuffer.size())
			return null;
		return recBuffer.get(index).first;
	}

	synchronized void setReadingDone(boolean isDone) {
		readingDone = isDone;
		setChanged();
		notifyObservers("READING_DONE");
	}

	synchronized public void start() {
		if (!readingDone || isAtBegin()) return;
		setReadingDone(false);
		navigatorThread = new AbsolutePosNavigator();
		navigatorThread.start();
	}

	synchronized public void end() {
		if (!readingDone || isAtEnd()) return;
		setReadingDone(false);
		navigatorThread = new AbsolutePosNavigator(logEndPos, false);
		navigatorThread.start();
	}

	synchronized public void next() {
		if (!readingDone || isAtEnd()) return;
		setReadingDone(false);
		navigatorThread = new RelativePosNavigator(true, regionSize);
		navigatorThread.start();
	}

	synchronized public void prev() {
		if (!readingDone || isAtBegin()) return;
		setReadingDone(false);
		navigatorThread = new RelativePosNavigator(false, regionSize);
		navigatorThread.start();
	}

	synchronized public void shiftUp() {
		if (!readingDone || isAtBegin()) return;
		setReadingDone(false);
		navigatorThread = new RelativePosNavigator(false, 1);
		navigatorThread.start();
	}

	synchronized public void shiftDown() {
		if (!readingDone || isAtEnd()) return;
		setReadingDone(false);
		navigatorThread = new RelativePosNavigator(true, 1);
		navigatorThread.start();
	}

	synchronized public void shiftTo(Position pos) {
		if (!readingDone || pos.equals(getPosition(0))) return;
		setReadingDone(false);
		navigatorThread = new AbsolutePosNavigator(pos, true);
		navigatorThread.start();
	}

	@Nullable
	synchronized public Position getPosition(int index) {
		if (index >= recBuffer.size()) return null;
		return recBuffer.get(index).second;
	}

	synchronized public int getRecordCount() {
		return recBuffer.size();
	}

	synchronized public int getRegionSize() {
		return regionSize;
	}

	synchronized public boolean isReadingDone() {
		return readingDone;
	}

	synchronized public boolean isAtBegin() {
		if (!readingDone) return false;
		if (recBuffer.size() == 0) return false;
		if (recBuffer.get(0).second.equals(logBeginPos) ) return true;
		return false;
	}

	synchronized public boolean isAtEnd() {
		if (!readingDone) return false;
		if (recBuffer.size() == 0) return false;
		if (recBuffer.get(recBuffer.size() - 1).second.equals(logEndPos) ) return true;
		return false;
	}

	Log getLog() {
		return this.log;
	}

	int getRecordSize() {
		return log.getMetadata().getFieldCount();
	}

	synchronized private void pushBegin(Pair<Record, Position> pair) {
		recBuffer.pushBegin(pair);
		setChanged();
		notifyObservers("ADD_TO_BEGIN");
	}

	synchronized private void pushEnd(Pair<Record, Position> pair) {
		recBuffer.pushEnd(pair);
		setChanged();
		notifyObservers("ADD_TO_END");
	}


	synchronized private void clear() {
		recBuffer.clear();
		setChanged();
		notifyObservers("CLEAR");
	}



}
