package org.lf.logs;

import org.lf.parser.Position;
import org.lf.util.Comparators;
import org.lf.util.Pair;
import org.lf.util.Triple;


import java.io.IOException;
import java.util.*;

import static org.lf.util.CollectionFactory.newHashMap;
import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.newLinkedList;

public class MergeLogs implements Log {
    private final Log[] logs;
    private final Integer[] timeFieldIndices;

    //used for comparing record columns
    private Comparator<String> timeComparator;

    private class PosRec extends Pair<Position,Record> {
        private PosRec(Position pos, Record rec) {
            super(pos, rec);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(obj.getClass() != PosRec.class) return false;
            //suppose that equal positions means equal records, so there is no need to compare records
            PosRec pr = (PosRec) obj;
            if(pr.first == null) return this.first == null;
            return pr.first.equals(this.first);
        }
    }

    private class MergedRecord implements Record {
        MergedPosition pos;
        int size = 0;
        private Map<Integer, Integer> our2origIndex;

        public MergedRecord(MergedPosition pos) {
            this.pos = pos;
        }

        @Override
        public String get(int index) {
            if (!getOur2origIndex().containsKey(index)) return null;
            CurPrevIndex entity = pos.cpisAscCur.first();
            return entity.first.second.get(getOur2origIndex().get(index));
        }

        //key is the index of merged record and value is the index of underlying record
        private Map<Integer, Integer> getOur2origIndex() {
            if (our2origIndex != null)
                return our2origIndex;

            our2origIndex = newHashMap();
            
            CurPrevIndex entity = pos.cpisAscCur.first();
            if (entity.third == 0) {
                for (int i =0 ; i < entity.first.second.size() ; ++i ) {
                    our2origIndex.put(i, i);
                }
            } else {
                int lengthSum = 0;
                for (int i = 0 ; i < entity.third; ++i) {
                    int recordLength = logs[i].getFields().length;
                    lengthSum += recordLength;
                }
                our2origIndex.put(timeFieldIndices[0], timeFieldIndices[entity.third]);
                int indexShift = lengthSum - entity.third + 1;
                for (int i =0 ; i < entity.first.second.size() ; ++i ) {
                    if (i < timeFieldIndices[entity.third])     our2origIndex.put(i + indexShift, i);
                    if (i == timeFieldIndices[entity.third])    continue;
                    if (i > timeFieldIndices[entity.third])     our2origIndex.put(i + indexShift - 1, i);
                }
            }
            return our2origIndex;
        }

        @Override
        public int size() {
            if  (size != 0) return size;

            for (Log log : logs) {
                size += log.getFields().length;
            }
            //one common field in every log
            size -= logs.length - 1;
            return size;
        }

    }

    private class CurPrevIndex extends Triple<PosRec,PosRec,Integer> {
        private CurPrevIndex(PosRec cur, PosRec prev, Integer index) {
            super(cur, prev, index);
        }
    }

    private abstract class CPIComparator implements Comparator<CurPrevIndex> {
        @Override
        public int compare(CurPrevIndex o1, CurPrevIndex o2) {
            PosRec pr1 = getPosRec(o1), pr2 = getPosRec(o2);
            if (pr1 == null && pr2 == null) return o1.third.compareTo(o2.third);
            if (pr1 == null) return 1;
            if (pr2 == null) return -1;

            int res =  timeComparator.compare(
                    pr1.second.get(timeFieldIndices[o1.third]),
                    pr2.second.get(timeFieldIndices[o2.third]));
            if (res == 0)
                res = o1.third.compareTo(o2.third);

            return res;
        }
        protected abstract PosRec getPosRec(CurPrevIndex cpi);
    }
    private Comparator<CurPrevIndex> COMPARE_CPI_ON_CUR = new CPIComparator() {
        protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.first; }
    };
    private Comparator<CurPrevIndex> COMPARE_CPI_ON_PREV = new CPIComparator() {
        protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.second; }
    };

    private class MergedPosition implements Position {
        TreeSet<CurPrevIndex> cpisAscCur;
        TreeSet<CurPrevIndex> cpisAscPrev;

        private MergedPosition(TreeSet<CurPrevIndex> cpisAscCur, TreeSet<CurPrevIndex> cpisAscPrev) {
            this.cpisAscCur = cpisAscCur;
            this.cpisAscPrev = cpisAscPrev;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null &&
            obj.getClass() == MergedPosition.class &&
            ((MergedPosition)obj).cpisAscCur.equals(this.cpisAscCur) &&
            ((MergedPosition)obj).cpisAscPrev.equals(this.cpisAscPrev);
        }
    }

    public MergeLogs(Log[] logs, Integer[] fields) {
        this(logs, fields, Comparators.<String>naturalOrder());
    }

    public MergeLogs(Log[] logs, Integer[] fields, Comparator<String> timeComparator) {
        this.logs = logs;
        this.timeFieldIndices = fields;
        this.timeComparator = timeComparator;
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
    public Position first() throws IOException {
        List<CurPrevIndex> p = newList();
        for(int i = 0; i < logs.length; ++i) {
            Position curPos = logs[i].first();
            PosRec cur = new PosRec(curPos, logs[i].readRecord(curPos));
            p.add(new CurPrevIndex(cur, null, i));
        }
        return new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV));
    }

    @Override
    public Position last() throws IOException {
        List<CurPrevIndex> p = newList();
        for(int i = 0; i < logs.length; ++i) {
            Position lastPos = logs[i].last();
            PosRec prev = new PosRec(lastPos, logs[i].readRecord(lastPos));
            p.add(new CurPrevIndex(null, prev , i));
        }
        return prev(new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV)));
    }

    @Override
    public Position next(Position pos) throws IOException {
        // Specification:
        // readRecord(next(p)) is the earliest record in the union of all records
        // in all logs, later than readRecord(p).

        MergedPosition p = (MergedPosition) pos;
        TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
        TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

        CurPrevIndex cur = curSortedCopy.first();
        curSortedCopy.remove(cur);
        prevSortedCopy.remove(cur);

        Position nextPos = logs[cur.third].next(cur.first.first);
        PosRec nextPair = new PosRec(nextPos, logs[cur.third].readRecord(nextPos));
        CurPrevIndex newEntity = new CurPrevIndex(nextPair, cur.first, cur.third);
        curSortedCopy.add(newEntity);
        prevSortedCopy.add(newEntity);

        return new MergedPosition(curSortedCopy, prevSortedCopy);
    }

    @Override
    public Position prev(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
        TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

        CurPrevIndex cur = prevSortedCopy.last();
        curSortedCopy.remove(cur);
        prevSortedCopy.remove(cur);

        Position prevPos = logs[cur.third].prev(cur.second.first);
        PosRec prevPair = new PosRec(prevPos, logs[cur.third].readRecord(prevPos));
        CurPrevIndex newEntity = new CurPrevIndex(cur.second, prevPair, cur.third);
        curSortedCopy.add(newEntity);
        prevSortedCopy.add(newEntity);
        return new MergedPosition(curSortedCopy, prevSortedCopy);
    }

    @Override
    public Record readRecord(Position pos) throws IOException {
        MergedPosition mPos = ((MergedPosition) pos);
        return new MergedRecord(mPos);
    }

    @Override
    public Field[] getFields() {
        List<Field> res = newLinkedList();
        for (int i = 0; i < logs.length; ++i) {
            Field[] curFields = logs[i].getFields();
            for (int j = 0; j < curFields.length; ++j) {
                if (i != 0 && timeFieldIndices[i] == j ) continue;
                res.add(curFields[j]);
            }
        }
        return res.toArray(new Field[res.size()]);
    }
}
