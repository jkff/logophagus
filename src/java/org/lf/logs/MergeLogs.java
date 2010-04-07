package org.lf.logs;

import org.lf.parser.Position;
import org.lf.util.Comparators;
import org.lf.util.Pair;
import org.lf.util.Triple;
import java.io.IOException;
import java.util.*;
import static org.lf.util.CollectionFactory.newList;

public class MergeLogs implements Log {
	private final Log[] logs;
	//pair<first position, last position>. it is not allowed to use this link to get borders because of it's lazy init.
	//use getLogBorders() method instead 
	private Pair<Position, Position>[] logsBorders;

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



	private class CurPrevIndex extends Triple<PosRec,PosRec,Integer> {
		private CurPrevIndex(PosRec cur, PosRec prev, Integer index) {
			super(cur, prev, index);
		}
	}

	private abstract class CPIComparator implements Comparator<CurPrevIndex> {
		@Override
		public int compare(CurPrevIndex o1, CurPrevIndex o2) {
			PosRec pr1 = getPosRec(o1), pr2 = getPosRec(o2);

			if(pr1 == null || pr2 == null)	
				return compareNullable(o1, o2);

			int res =  timeComparator.compare(
					(String)pr1.second.getCellValues()[timeFieldIndices[o1.third]],
					(String)pr2.second.getCellValues()[timeFieldIndices[o1.third]]);

			if (res != 0)	return res; 
			return o1.third.compareTo(o2.third);
		}
		protected abstract PosRec getPosRec(CurPrevIndex cpi);

		protected abstract int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2);
	}

	private Comparator<CurPrevIndex> COMPARE_CPI_ON_CUR = new CPIComparator() {
		protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.first; }

		@Override
		public int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2) {
			PosRec pr1 = getPosRec(cpi1), pr2 = getPosRec(cpi2);
			if (pr1 == null && pr2 == null) return cpi1.third.compareTo(cpi2.third);
			if (pr1 == null) return 1;
			if (pr2 == null) return -1;
			throw new IllegalArgumentException("Expected one of the two PosRec's to be null");
		}
	};

	private Comparator<CurPrevIndex> COMPARE_CPI_ON_PREV = new CPIComparator() {
		protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.second; }

		@Override
		public int compareNullable(CurPrevIndex cpi1, CurPrevIndex cpi2) {
			PosRec pr1 = getPosRec(cpi1), pr2 = getPosRec(cpi2);
			if (pr1 == null && pr2 == null) return -cpi1.third.compareTo(cpi2.third);
			if (pr1 == null) return -1;
			if (pr2 == null) return 1;
			throw new IllegalArgumentException("Expected one of the two PosRec's to be null");
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
			((MergedPosition)obj).cpisAscCur.equals(this.cpisAscCur) &&
			((MergedPosition)obj).cpisAscPrev.equals(this.cpisAscPrev);
		}

		@Override
		public Log getCorrespondingLog() {
			return MergeLogs.this;
		}
	}

	public MergeLogs(Log[] logs, Integer[] fields) throws IOException {
		this(logs, fields, Comparators.<String>naturalOrder());
	}

	public MergeLogs(Log[] logs, Integer[] fields, Comparator<String> timeComparator) throws IOException {
		this.logs = logs;
		this.logsBorders = null;
		this.timeFieldIndices = fields;
		this.timeComparator = timeComparator;
		logsBorders = new Pair[logs.length];
		for (int i = 0; i < logs.length; ++i) {
			logsBorders[i] = new Pair<Position, Position>(logs[i].first(), logs[i].last());
		}
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
			Position curPos = logsBorders[i].first;
			PosRec cur = new PosRec(curPos, logs[i].readRecord(curPos));
			p.add(new CurPrevIndex(cur, null, i));
		}
		return new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV));
	}

	@Override
	public Position last() throws IOException {
		List<CurPrevIndex> p = newList();
		for(int i = 0; i < logs.length; ++i) {
			Position lastPos = logsBorders[i].second;
			PosRec prev = new PosRec(lastPos, logs[i].readRecord(lastPos));
			p.add(new CurPrevIndex(null, prev , i));
		}
		return prev(new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV)));
	}

	@Override
	public Position next(Position pos) throws IOException {
		MergedPosition p = (MergedPosition)pos;
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
		if (cur.first == null) return null; 
		Position nextPos = logsBorders[cur.third].second.equals(cur.first.first) ? null : logs[cur.third].next(cur.first.first);       
		PosRec nextPair = (nextPos==null ? null : new PosRec(nextPos, logs[cur.third].readRecord(nextPos)) );
		CurPrevIndex newEntity = new CurPrevIndex(nextPair, cur.first, cur.third);
		curSortedCopy.add(newEntity);
		prevSortedCopy.add(newEntity);

		return new MergedPosition(curSortedCopy, prevSortedCopy);
	}

	@Override
	public Position prev(Position pos) throws IOException {
		MergedPosition p = (MergedPosition)pos;

		if (p == null) 
			throw new IllegalArgumentException("Unsupported position for this log.");

		TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
		TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

		CurPrevIndex cur = prevSortedCopy.last();
		curSortedCopy.remove(cur);
		prevSortedCopy.remove(cur);
		if (cur.second == null) return null;
		Position prevPos = logsBorders[cur.third].first.equals(cur.second.first)? null : logs[cur.third].prev(cur.second.first);
		PosRec prevPair = (prevPos==null ? null : new PosRec(prevPos, logs[cur.third].readRecord(prevPos)) );
		CurPrevIndex newEntity = new CurPrevIndex(cur.second, prevPair, cur.third);
		curSortedCopy.add(newEntity);
		prevSortedCopy.add(newEntity);
		return new MergedPosition(curSortedCopy, prevSortedCopy);
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		MergedPosition p = (MergedPosition)pos;
		if (p == null) 
			throw new IllegalArgumentException("Can't read record corresponding to such pos.");
		return p.cpisAscCur.first().first.second;
	}


	@Override
	public Position convertToNative(Position p) throws IOException{
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

		MergedPosition curPos = (MergedPosition)this.first();
		MergedPosition lastPos = (MergedPosition)this.last();

		while (	!(	curPos.cpisAscCur.first().third == logOfPos	&&
				curPos.cpisAscCur.first().first.first.equals(p))
		) 
		{
			if (curPos.equals(lastPos)) return null;
			curPos = (MergedPosition) next(curPos);
		}

		return curPos;
	}

	@Override
	public Format[] getFormats() {
		// TODO Auto-generated method stub
		return null;
	}


}
