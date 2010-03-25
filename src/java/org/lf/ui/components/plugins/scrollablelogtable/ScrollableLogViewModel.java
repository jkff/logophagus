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
                if (logBeginPos == null)    logBeginPos = log.first();
                if (logBeginPos == null)    return;
                if (logEndPos == null)      logEndPos = log.last();
                if (fromWhere == null)      fromWhere = logBeginPos;

                if (recordsToRead == regionSize)    clear();

//                if (readFromWhere && fromWhere.getCorrespondingLog() != log) {
//                	Position temp = log.next(fromWhere);
//                	if (temp != null) { 
//                		fromWhere = log.prev(temp);
//                	} else { 
//                		temp = log.prev(fromWhere);
//                		if (temp == null)
//                			return;
//                		fromWhere = log.next(temp);
//                	}
//                }
                
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
                    if (tempPos == null ||  (i == recordsToRead - 1 && readFromWhere))
                        break;
                    if (directionForward)
                        pushEnd(pair(log.readRecord(tempPos), tempPos));
                    else
                        pushBegin(pair(log.readRecord(tempPos), tempPos));
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
    }

    @Nullable
    public Record getRecord(int index) {
        if (index >= recBuffer.size()) return null;
        return recBuffer.get(index).first;
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
        if (pair.first.size() > getRecordSize()) {
            setChanged();
            notifyObservers("CHANGE_RECORD_SIZE");
        }
        recBuffer.pushBegin(pair);
        setChanged();
        notifyObservers("ADD_BEGIN");
    }

    synchronized private void pushEnd(Pair<Record, Position> pair) {
        if (pair.first.size() > getRecordSize()) {
            setChanged();
            notifyObservers("CHANGE_RECORD_SIZE");
        }
        recBuffer.pushEnd(pair);
        setChanged();
        notifyObservers("ADD_END");
    }


    synchronized private void clear() {
        recBuffer.clear();
        setChanged();
        notifyObservers("CLEAR");
    }



}
