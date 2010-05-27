package org.lf.ui.components.plugins.scrollablelog;

import com.sun.istack.internal.Nullable;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.util.Pair;

import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lf.util.CollectionFactory.pair;

public class ScrollableLogModel extends Observable {
    private final ExecutorService executor;
    private final CyclicBuffer<Pair<Record, Position>> recBuffer;
    private final int regionSize;
    private final Log log;
    private Position logBeginPos;
    private Position logEndPos;
    private boolean readingDone;
    private volatile float progress = 0F;

    private abstract class Navigator implements Runnable {
        // adds records to model. startPos record included
        final int addRecordsToModel(Position startPos, int recordsCount, boolean isForward, float progressIncrement)
                throws IOException
        {
            Position temp = startPos;
            int counter = 1;
            for (; counter <= recordsCount; ++counter) {
                if (isForward) {
                    ScrollableLogModel.this.pushEnd(pair(log.readRecord(temp), temp));
                    if (logEndPos.equals(temp))
                        break;
                    temp = log.next(temp);
                } else {
                    ScrollableLogModel.this.pushBegin(pair(log.readRecord(temp), temp));
                    if (logBeginPos.equals(temp))
                        break;
                    temp = log.prev(temp);
                }
                progress += progressIncrement;
            }
            return counter;
        }
    }


    private class RelativePosNavigator extends Navigator {
        private final boolean isForward;
        private final Position fromWhere;
        private final int recordsToRead;

        public RelativePosNavigator(boolean isForward, int recordsToRead) {
            this.fromWhere = getPosition(isForward ? getRecordCount() - 1 : 0);
            this.isForward = isForward;
            this.recordsToRead = recordsToRead;
        }

        @Override
        public void run() {
            try {
                progress = 0;
                Position tempPos;
                if (isForward) {
                    if (logEndPos.equals(fromWhere)) return;
                    tempPos = log.next(fromWhere);
                } else {
                    if (logBeginPos.equals(fromWhere)) return;
                    tempPos = log.prev(fromWhere);
                }
                addRecordsToModel(tempPos, recordsToRead, isForward, 100F / recordsToRead);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                progress = 100F;
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
                progress = 0;
                if (logBeginPos == null) logBeginPos = log.first();
                if (logBeginPos == null) return;
                if (logEndPos == null) logEndPos = log.last();
                if (logEndPos == null) return;
                if (fromWhere == null) fromWhere = logBeginPos;
                clear();

                int recordsRead = addRecordsToModel(fromWhere, regionSize, isForward, 100F / regionSize);
                if (recordsRead != regionSize) {
                    //now we will read missing records moving in reverse direction
                    Position tempPos;
                    if (isForward) {
                        if (logBeginPos.equals(fromWhere)) return;
                        tempPos = log.prev(fromWhere);
                    } else {
                        if (logEndPos.equals(fromWhere)) return;
                        tempPos = log.next(fromWhere);
                    }
                    addRecordsToModel(tempPos, regionSize - recordsRead, !isForward, 100F / regionSize);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                progress = 100F;
                setReadingDone(true);
            }
        }
    }


    public ScrollableLogModel(Log log, int regionSize) {
        this.log = log;
        this.regionSize = regionSize;
        this.readingDone = true;
        this.executor = Executors.newSingleThreadExecutor();
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
        notifyObservers("READING_STATE");
    }

    synchronized public void start() {
        if (isAtBegin())
            return;
        setReadingDone(false);
        executor.execute(new AbsolutePosNavigator());
    }

    synchronized public void end() {
        if (isAtEnd())
            return;
        setReadingDone(false);
        executor.execute(new AbsolutePosNavigator(logEndPos, false));
    }

    synchronized public void next() {
        if (isAtEnd())
            return;
        setReadingDone(false);
        executor.execute(new RelativePosNavigator(true, regionSize));
    }

    synchronized public void prev() {
        if (isAtBegin())
            return;
        setReadingDone(false);
        executor.execute(new RelativePosNavigator(false, regionSize));
    }

    synchronized public void shiftUp() {
        if (isAtBegin())
            return;
        setReadingDone(false);
        // Here and below we navigate an even number of records (for example, 2).
        // This is a hack to ensure that the alternating white/gray coloring in
        // RecordRenderer remains consistent while the list is being scrolled.
        // It could be done in a more elegant way, I suppose, but this hack is so
        // innocent that I let it be.
        executor.execute(new RelativePosNavigator(false, 2));
    }

    synchronized public void shiftDown() {
        if (isAtEnd())
            return;
        setReadingDone(false);
        executor.execute(new RelativePosNavigator(true, 2));
    }

    synchronized public void shiftTo(Position pos) {
        if (pos.equals(getPosition(0)))
            return;
        setReadingDone(false);
        executor.execute(new AbsolutePosNavigator(pos, true));
    }

    @Nullable
    synchronized public Position getPosition(int index) {
        if (index >= recBuffer.size())
            return null;
        return recBuffer.get(index).second;
    }

    synchronized public int getRecordCount() {
        return recBuffer.size();
    }

    synchronized public boolean isReadingDone() {
        return readingDone;
    }

    synchronized public boolean isAtBegin() {
        return readingDone && recBuffer.size() != 0 && recBuffer.get(0).second.equals(logBeginPos);
    }

    synchronized public boolean isAtEnd() {
        return readingDone && recBuffer.size() != 0 && recBuffer.get(recBuffer.size() - 1).second.equals(logEndPos);
    }


    public float getProgress() {
        return progress;
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
