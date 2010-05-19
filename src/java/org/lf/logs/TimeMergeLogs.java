package org.lf.logs;

import org.joda.time.DateTime;
import org.lf.parser.Position;
import org.lf.util.Pair;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.lf.util.CollectionFactory.newHashSet;
import static org.lf.util.CollectionFactory.newList;

public class TimeMergeLogs implements Log {
    private final Log[] logs;
    private final Format[] mergeFormats;
    private final Pair<Position, Position>[] logsBorders;
    private MergedPosition cachedFirst;
    private MergedPosition cachedLast;

    private class CurPrevIndex {
        private Position cur;
        private Position prev;
        private DateTime curTime;
        private DateTime prevTime;
        private Integer index;

        private CurPrevIndex(Position cur, Position prev, DateTime curTime, DateTime prevTime, Integer index) {
            this.cur = cur;
            this.prev = prev;
            this.curTime = curTime;
            this.prevTime = prevTime;
            this.index = index;
        }

        public DateTime getCurTime() {
            if (curTime == null) {
                try {
                    curTime = logs[index].getTime(cur);
                } catch (IOException e) {
                    // Ignore
                    e.printStackTrace();
                }
            }
            return curTime;
        }

        public DateTime getPrevTime() {
            if (prevTime == null) {
                try {
                    prevTime = logs[index].getTime(prev);
                } catch (IOException e) {
                    // Ignore
                    e.printStackTrace();
                }
            }
            return prevTime;
        }
    }

    private abstract class CPIComparator implements Comparator<CurPrevIndex> {
        @Override
        public int compare(final CurPrevIndex o1, final CurPrevIndex o2) {
            Position pos1 = getPosition(o1), pos2 = getPosition(o2);

            if (pos1 == null || pos2 == null)
                return compareNullable(o1, o2);

            DateTime dt1 = getTime(o1);
            DateTime dt2 = getTime(o2);
            int res = dt1.compareTo(dt2);
            if (res != 0) return res;

            return o1.index.compareTo(o2.index);
        }

        protected abstract Position getPosition(CurPrevIndex cpi);

        protected abstract DateTime getTime(CurPrevIndex cpi);

        protected abstract int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2);
    }

    private Comparator<CurPrevIndex> COMPARE_CPI_ON_CUR = new CPIComparator() {
        protected Position getPosition(CurPrevIndex cpi) {
            return cpi.cur;
        }

        protected DateTime getTime(CurPrevIndex cpi) {
            return cpi.getCurTime();
        }

        @Override
        public int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2) {
            Position pos1 = getPosition(cpi1), pos2 = getPosition(cpi2);
            if (pos1 == null && pos2 == null) return cpi1.index.compareTo(cpi2.index);
            if (pos1 == null) return 1;
            if (pos2 == null) return -1;
            throw new IllegalArgumentException("Expected one of the two Position's to be null");
        }
    };

    private Comparator<CurPrevIndex> COMPARE_CPI_ON_PREV = new CPIComparator() {
        protected Position getPosition(CurPrevIndex cpi) {
            return cpi.prev;
        }

        protected DateTime getTime(CurPrevIndex cpi) {
            return cpi.getPrevTime();
        }

        @Override
        public int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2) {
            Position pos1 = getPosition(cpi1), pos2 = getPosition(cpi2);
            if (pos1 == null && pos2 == null) return -cpi1.index.compareTo(cpi2.index);
            if (pos1 == null) return -1;
            if (pos2 == null) return 1;
            throw new IllegalArgumentException("Expected one of the two Position's to be null");
        }
    };

    private class MergedPosition implements Position {
        final TreeSet<CurPrevIndex> cpisAscCur;
        final TreeSet<CurPrevIndex> cpisAscPrev;

        private MergedPosition(TreeSet<CurPrevIndex> cpisAscCur, TreeSet<CurPrevIndex> cpisAscPrev) {
            this.cpisAscCur = cpisAscCur;
            this.cpisAscPrev = cpisAscPrev;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null &&
                    obj.getClass() == MergedPosition.class &&
                    ((MergedPosition) obj).cpisAscCur.equals(this.cpisAscCur) &&
                    ((MergedPosition) obj).cpisAscPrev.equals(this.cpisAscPrev);
        }

        @Override
        public Log getCorrespondingLog() {
            return TimeMergeLogs.this;
        }
    }

    public TimeMergeLogs(Log[] logs) throws IOException {
        this.logs = logs;
        this.logsBorders = new Pair[logs.length];
        for (int i = 0; i < logs.length; ++i) {
            logsBorders[i] = new Pair<Position, Position>(logs[i].first(), logs[i].last());
        }

        Set<Format> formatSet = newHashSet();
        for (Log log : logs) {
            for (Format format : log.getFormats()) {
                formatSet.add(format);
            }
        }
        this.mergeFormats = formatSet.toArray(new Format[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Merge:");
        for (Log cur : logs) {
            sb.append(cur).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    private TreeSet<CurPrevIndex> fromList(List<CurPrevIndex> entities, Comparator<CurPrevIndex> comp) {
        TreeSet<CurPrevIndex> q = new TreeSet<CurPrevIndex>(comp);
        for (CurPrevIndex p : entities) q.add(p);
        return q;
    }

    @Override
    public synchronized Position first() throws IOException {
        if (cachedFirst == null) {
            List<CurPrevIndex> p = newList();
            for (int i = 0; i < logs.length; ++i) {
                Position firstPos = logsBorders[i].first;
                p.add(new CurPrevIndex(firstPos, null, null, null, i));
            }
            cachedFirst = new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV));
        }

        return cachedFirst;
    }

    @Override
    public synchronized Position last() throws IOException {
        if (cachedLast == null) {
            List<CurPrevIndex> p = newList();
            for (int i = 0; i < logs.length; ++i) {
                Position lastPos = logsBorders[i].second;
                p.add(new CurPrevIndex(null, lastPos, null, null, i));
            }
            cachedLast = (MergedPosition) prev(new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV)));
        }
        return cachedLast;
    }

    @Override
    public Position next(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        // Specification:
        // readRecord(next(p)) is the earliest record in the union of all records
        // in all logs, later than readRecord(p).
        if (p == null)
            throw new IllegalArgumentException("Unsupported position for this log.");

        TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
        TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

        CurPrevIndex cur = curSortedCopy.first();
        curSortedCopy.remove(cur);
        prevSortedCopy.remove(cur);
        //check if it's not the position of merged log end
        if (cur.cur == null)
            return null;
        Position nextPos = logsBorders[cur.index].second.equals(cur.cur) ? null : logs[cur.index].next(cur.cur);
        CurPrevIndex newEntity = new CurPrevIndex(nextPos, cur.cur, null, cur.getCurTime(), cur.index);
        curSortedCopy.add(newEntity);
        prevSortedCopy.add(newEntity);

        return new MergedPosition(curSortedCopy, prevSortedCopy);
    }

    @Override
    public Position prev(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;

        if (p == null)
            throw new IllegalArgumentException("Unsupported position for this log.");

        TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
        TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

        CurPrevIndex cur = prevSortedCopy.last();
        curSortedCopy.remove(cur);
        prevSortedCopy.remove(cur);
        if (cur.prev == null) return null;
        Position prevPos = logsBorders[cur.index].first.equals(cur.prev) ? null : logs[cur.index].prev(cur.prev);
        CurPrevIndex newEntity = new CurPrevIndex(cur.prev, prevPos, cur.getPrevTime(), null, cur.index);
        curSortedCopy.add(newEntity);
        prevSortedCopy.add(newEntity);
        return new MergedPosition(curSortedCopy, prevSortedCopy);
    }

    @Override
    public Record readRecord(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        if (p == null)
            throw new IllegalArgumentException("Can't read record corresponding to such pos.");
        return logs[p.cpisAscCur.first().index].readRecord(p.cpisAscCur.first().cur);
    }


    @Override
    public Position convertToNative(Position p) throws IOException {
        if (p == null) return null;
        if (p.getCorrespondingLog() == this) return p;

        int logOfPos = -1;
        for (int i = 0; i < logs.length; ++i) {
            if (logs[i] == p.getCorrespondingLog()) {
                logOfPos = i;
                break;
            }
        }

        if (logOfPos == -1)
            return null;

        MergedPosition curPos = (MergedPosition) this.first();
        MergedPosition lastPos = (MergedPosition) this.last();

        while (!(curPos.cpisAscCur.first().index == logOfPos && curPos.cpisAscCur.first().cur.equals(p))) {
            if (curPos.equals(lastPos)) return null;
            curPos = (MergedPosition) next(curPos);
        }

        return curPos;
    }

    @Override
    public Position convertToParent(Position pos) throws IOException {
        if (pos == null || pos.getCorrespondingLog() != this) return null;
        MergedPosition mPos = (MergedPosition) pos;
        return mPos.cpisAscCur.first().cur;
    }

    @Override
    public Format[] getFormats() {
        return mergeFormats;
    }

    @Override
    public DateTime getTime(Position pos) throws IOException {
        MergedPosition mPos = (MergedPosition) pos;
        return logs[mPos.cpisAscCur.first().index].getTime(mPos.cpisAscCur.first().cur);
    }

}
