package org.lf.ui.components.plugins.scrollablelog;

import com.sun.istack.internal.Nullable;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.pair;

public class ScrollableLogModel extends Observable {
    private final ExecutorService executor;
    private final CyclicBuffer<Pair<Position, Record>> recBuffer;
    private final int regionSize;
    private final Log log;
    private volatile float progress = 0F;

    private volatile Navigator curNavigator = null;
    private boolean readingDone;

    // Thrown inside a navigation task when a new navigation task supersedes it.
    private static class NavigationObsoletedException extends Exception {}
    
    private static abstract class Navigator implements Runnable {
        private final ScrollableLogModel model;

        protected Navigator(ScrollableLogModel model) {
            this.model = model;
        }

        public void run() {
            try {
                setProgress(0);
                runImpl();
            } catch (IOException e) {
                e.printStackTrace();
            } catch(NavigationObsoletedException e) {
                // Ignore
            } finally {
                setProgress(100F);
            }
        }

        protected abstract void runImpl() throws IOException, NavigationObsoletedException;

        // adds records to model. startPos record included
        final int addRecordsToModel(Position startPos, int recordsCount, boolean isForward, float progressIncrement)
                throws IOException, NavigationObsoletedException
        {
            Position temp = startPos;
            int counter = 1;
            for (; counter <= recordsCount; ++counter) {
                if (isForward) {
                    this.pushEnd(pair(temp, model.log.readRecord(temp)));
                    if (getLog().last().equals(temp))
                        break;
                    temp = model.log.next(temp);
                } else {
                    this.pushBegin(pair(temp, model.log.readRecord(temp)));
                    if (getLog().first().equals(temp))
                        break;
                    temp = model.log.prev(temp);
                }

                incrementProgress(progressIncrement);
            }
            return counter;
        }

        protected Log getLog() {
            return model.log;
        }

        protected void pushBegin(Pair<Position,Record> posAndRec) throws NavigationObsoletedException {
            synchronized(model) {
                if(model.curNavigator != this)
                    throw new NavigationObsoletedException();
                model.pushBegin(posAndRec);
            }
        }

        protected void pushEnd(Pair<Position,Record> posAndRec) throws NavigationObsoletedException {
            synchronized(model) {
                if(model.curNavigator != this)
                    throw new NavigationObsoletedException();
                model.pushEnd(posAndRec);
            }
        }

        protected void clear() throws NavigationObsoletedException {
            synchronized(model) {
                if(model.curNavigator != this)
                    throw new NavigationObsoletedException();
                model.clear();
            }
        }

        protected void setProgress(float progress) {
            synchronized(model) {
                if(model.curNavigator != this)
                    return;
                model.setProgress(progress);
            }
        }

        protected void incrementProgress(float progressIncrement) {
            synchronized(model) {
                if(model.curNavigator != this)
                    return;
                model.setProgress(model.progress + progressIncrement);
            }
        }

        protected int getRegionSize() {
            return model.regionSize;
        }
    }

    private static class RelativePosNavigator extends Navigator {
        private final boolean isForward;
        private final Position fromWhere;
        private final int recordsToRead;
        
        public RelativePosNavigator(ScrollableLogModel model, Position fromWhere, boolean isForward, int recordsToRead) {
            super(model);
            
            this.fromWhere = fromWhere;
            this.isForward = isForward;
            this.recordsToRead = recordsToRead;
        }

        @Override
        protected void runImpl() throws IOException, NavigationObsoletedException {
            Position tempPos;
            if (isForward) {
                if (getLog().last().equals(fromWhere)) return;
                tempPos = getLog().next(fromWhere);
            } else {
                if (getLog().first().equals(fromWhere)) return;
                tempPos = getLog().prev(fromWhere);
            }
            addRecordsToModel(tempPos, recordsToRead, isForward, 100F / recordsToRead);
        }
    }

    private static class AbsolutePosNavigator extends Navigator {
        private final boolean isForward;
        private Position fromWhere;
        // if this constructor is used then reading will be done from log start
        public AbsolutePosNavigator(ScrollableLogModel model) {
            this(model, null, true);
        }

        public AbsolutePosNavigator(ScrollableLogModel model, Position fromWhere, boolean isForward) {
            super(model);
            this.fromWhere = fromWhere;
            this.isForward = isForward;
        }

        @Override
        public void runImpl() throws IOException, NavigationObsoletedException {
            if (getLog().first() == null)
                return;
            if (getLog().last() == null)
                return;
            if (fromWhere == null)
                fromWhere = getLog().first();
            
            clear();

            int regionSize = getRegionSize();

            int recordsRead = addRecordsToModel(fromWhere, regionSize, isForward, 100F / regionSize);
            if (recordsRead != regionSize) {
                //now we will read missing records moving in reverse direction
                Position tempPos;
                if (isForward) {
                    if (getLog().first().equals(fromWhere)) return;
                    tempPos = getLog().prev(fromWhere);
                } else {
                    if (getLog().last().equals(fromWhere)) return;
                    tempPos = getLog().next(fromWhere);
                }
                addRecordsToModel(tempPos, regionSize - recordsRead, !isForward, 100F / regionSize);
            }
        }
    }

    private static class SavedNavigator extends Navigator {
        private List<Pair<Position, Record>> shownContent;

        public SavedNavigator(ScrollableLogModel model, List<Pair<Position, Record>> shownContent) {
            super(model);
            this.shownContent = shownContent;
        }

        @Override
        protected void runImpl() throws IOException, NavigationObsoletedException {
            clear();
            for (Pair<Position, Record> posRec : shownContent) {
                pushEnd(posRec);
            }
        }
    }

    public ScrollableLogModel(Log log, int regionSize) {
        this.log = log;
        this.regionSize = regionSize;
        this.executor = Executors.newSingleThreadExecutor();
        this.recBuffer = new CyclicBuffer<Pair<Position, Record>>(regionSize);
        this.start();
    }


    synchronized public List<Pair<Position,Record>> getShownContent() {
        List<Pair<Position,Record>> res = newList();
        for(int i = 0; i < recBuffer.size(); ++i) {
            res.add(recBuffer.get(i));
        }
        return res;
    }

    synchronized public void setShownContent(List<Pair<Position, Record>> shownContent) {
        navigate(new SavedNavigator(this, shownContent));
    }

    @Nullable
    synchronized public Record getRecord(int index) {
        if (index >= recBuffer.size())
            return null;
        return recBuffer.get(index).second;
    }

    synchronized public void start() {
        if (!isAtBegin())
            navigate(new AbsolutePosNavigator(this));
    }

    synchronized public void end() {
        if (!isAtEnd()) {
            try {
                navigate(new AbsolutePosNavigator(this, log.last(), false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void next() {
        if (!isAtEnd())
            navigate(new RelativePosNavigator(this, getPosition(getShownRecordCount() - 1), true, regionSize));
    }

    synchronized public void prev() {
        if (!isAtBegin())
            navigate(new RelativePosNavigator(this, getPosition(0), false, regionSize));
    }

    synchronized public void shiftUp() {
        if (isAtBegin())
            return;
        // Here and below we navigate an even number of records (for example, 2).
        // This is a hack to ensure that the alternating white/gray coloring in
        // RecordRenderer remains consistent while the list is being scrolled.
        // It could be done in a more elegant way, I suppose, but this hack is so
        // innocent that I let it be.
        navigate(new RelativePosNavigator(this, getPosition(0), false, 2));
    }

    synchronized public void shiftDown() {
        if (!isAtEnd())
            navigate(new RelativePosNavigator(this, getPosition(getShownRecordCount() - 1), true, 2));
    }

    synchronized public void shiftTo(Position pos) {
        if (pos.equals(getPosition(0)))
            return;
        navigate(new AbsolutePosNavigator(this, pos, true));
    }

    private void navigate(Navigator navigator) {
        curNavigator = navigator;
        executor.execute(navigator);
    }

    @Nullable
    synchronized public Position getPosition(int index) {
        if (index >= recBuffer.size())
            return null;
        return recBuffer.get(index).first;
    }

    synchronized public int getShownRecordCount() {
        return recBuffer.size();
    }

    synchronized public boolean isAtBegin() {
        try {
            return recBuffer.size() != 0 &&
                   recBuffer.get(0).second.equals(log.first());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized void setReadingDone(boolean isDone) {
        readingDone = isDone;
        setChanged();
        notifyObservers("READING_STATE");
    }

    synchronized public boolean isReadingDone() {
        return readingDone;
    }
    
    synchronized public boolean isAtEnd() {
        try {
            return recBuffer.size() != 0 &&
                   recBuffer.get(recBuffer.size() - 1).second.equals(log.last());
        } catch (IOException e) {
            return false;
        }
    }

    synchronized public float getProgress() {
        return progress;
    }

    synchronized private void setProgress(float progress) {
        this.progress = progress;
        setChanged();
        notifyObservers("PROGRESS");
    }

    synchronized private void pushBegin(Pair<Position, Record> posAndRec) {
        recBuffer.pushBegin(posAndRec);
        setChanged();
        notifyObservers("ADD_TO_BEGIN");
    }

    synchronized private void pushEnd(Pair<Position, Record> posAndRec) {
        recBuffer.pushEnd(posAndRec);
        setChanged();
        notifyObservers("ADD_TO_END");
    }

    synchronized private void clear() {
        recBuffer.clear();
        setChanged();
        notifyObservers("CLEAR");
    }


}
