package org.lf.logs;

import org.lf.logs.Cell.Type;
import org.lf.parser.LogMetadata;
import org.lf.parser.Position;
import org.lf.util.Comparators;
import org.lf.util.Pair;
import org.lf.util.Triple;

import com.sun.istack.internal.Nullable;


import java.io.IOException;
import java.util.*;

import static org.lf.util.CollectionFactory.newHashMap;
import static org.lf.util.CollectionFactory.newList;

import static org.lf.util.CollectionFactory.newLinkedList;

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


	private class MergedCell extends Cell {
		private final int index;
		private final String name;
		private final Type type;
		private final Object value;

		private MergedCell(String name, Object value, Type type, int index) {
			this.index = index;
			this.name = name;
			this.value = value;
			this.type = type;
		}
		
		@Override
		public int getIndexInRecord() {
			return index;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
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
		public Cell getField(int index) {
			if (index >= size()) return null;
			if (!getOur2origIndex().containsKey(index)) return new MergedCell("null", null, Type.TEXT, index);
			CurPrevIndex entity = pos.cpisAscCur.first();
			Cell originalCell = entity.first.second.getCells()[getOur2origIndex().get(index)];
			return new MergedCell(originalCell.getName(), originalCell.getValue(), originalCell.getType(), index);
		}

		//map<key,value> : key is the index of merged record , value is the index of underlying record
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
					int recordLength = logs[i].getMetadata().getFieldCount();
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
				size += log.getMetadata().getFieldCount();
			}
			//one common field in every log
			size -= logs.length - 1;
			return size;
		}

		@Override
		public Cell[] getCells() {
			Cell[] cells = new Cell[size()];
			for(int i = 0 ; i < size(); ++i) {
				cells[i] = getField(i);
			}
			return cells;
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
                return compareNullable(pr1,pr2);
			
			int res =  timeComparator.compare(
					(String)pr1.second.getField(timeFieldIndices[o1.third]).getValue(),
					(String)pr2.second.getField(timeFieldIndices[o1.third]).getValue());
			if (res == 0)
				res = o1.third.compareTo(o2.third);

			return res;
		}
		protected abstract PosRec getPosRec(CurPrevIndex cpi);

		protected abstract int compareNullable(PosRec pr1, PosRec pr2);
	}
	
	private Comparator<CurPrevIndex> COMPARE_CPI_ON_CUR = new CPIComparator() {
		protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.first; }

		@Override
		public int compareNullable(PosRec pr1, PosRec pr2) {
			if (pr1 == null && pr2 == null) return 0;
			if (pr1 == null) return 1;
			if (pr2 == null) return -1;
			throw new IllegalArgumentException("Expected one of the two PosRec's to be null");
		}
	};
	
	private Comparator<CurPrevIndex> COMPARE_CPI_ON_PREV = new CPIComparator() {
		protected PosRec getPosRec(CurPrevIndex cpi) { return cpi.second; }

		@Override
		public int compareNullable(PosRec pr1, PosRec pr2) {
			if (pr1 == null && pr2 == null) return 0;
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

	public MergeLogs(Log[] logs, Integer[] fields) {
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
			Position curPos = getLogBorders(i).first;
			PosRec cur = new PosRec(curPos, logs[i].readRecord(curPos));
			p.add(new CurPrevIndex(cur, null, i));
		}
		return new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV));
	}

	@Override
	public Position last() throws IOException {
		List<CurPrevIndex> p = newList();
		for(int i = 0; i < logs.length; ++i) {
			Position lastPos = getLogBorders(i).second;
			PosRec prev = new PosRec(lastPos, logs[i].readRecord(lastPos));
			p.add(new CurPrevIndex(null, prev , i));
		}
		return prev(new MergedPosition(fromList(p, COMPARE_CPI_ON_CUR), fromList(p, COMPARE_CPI_ON_PREV)));
	}

	@Override
	public Position next(Position pos) throws IOException {
		MergedPosition p = getMergedPosition(pos);
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

		Position nextPos = cur.first.first.equals(getLogBorders(cur.third).second) ? null : logs[cur.third].next(cur.first.first);       
		PosRec nextPair = (nextPos==null ? null : new PosRec(nextPos, logs[cur.third].readRecord(nextPos)) );
		CurPrevIndex newEntity = new CurPrevIndex(nextPair, cur.first, cur.third);
		curSortedCopy.add(newEntity);
		prevSortedCopy.add(newEntity);

		return new MergedPosition(curSortedCopy, prevSortedCopy);
	}

	@Override
	public Position prev(Position pos) throws IOException {
		MergedPosition p = getMergedPosition(pos);

		if (p == null) 
			throw new IllegalArgumentException("Unsupported position for this log.");

		TreeSet<CurPrevIndex> curSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscCur);
		TreeSet<CurPrevIndex> prevSortedCopy = new TreeSet<CurPrevIndex>(p.cpisAscPrev);

		CurPrevIndex cur = prevSortedCopy.last();
		curSortedCopy.remove(cur);
		prevSortedCopy.remove(cur);
		Position prevPos = getLogBorders(cur.third).first.equals(cur.second.first)? null : logs[cur.third].prev(cur.second.first);
		PosRec prevPair = (prevPos==null ? null : new PosRec(prevPos, logs[cur.third].readRecord(prevPos)) );
		CurPrevIndex newEntity = new CurPrevIndex(cur.second, prevPair, cur.third);
		curSortedCopy.add(newEntity);
		prevSortedCopy.add(newEntity);
		return new MergedPosition(curSortedCopy, prevSortedCopy);
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		MergedPosition p = getMergedPosition(pos);
		if (p == null) 
			throw new IllegalArgumentException("Can't read record corresponding to such pos.");
		return new MergedRecord(p);
	}

	@Override
	public LogMetadata getMetadata() {
		final List<String> res = newLinkedList();
		for (int i = 0; i < logs.length; ++i) {
			String[] fieldNames = logs[i].getMetadata().getFieldNames();
			for (int j = 0; j < fieldNames.length; ++j) {
				if (i != 0 && timeFieldIndices[i] == j ) continue;
				res.add(fieldNames[j]);
			}
		}
		
		return new LogMetadata() {
			
			@Override
			public String[] getFieldNames() {
				return res.toArray(new String[0]);
			}
			
			@Override
			public String getFieldName(int fieldIndex) {
				return res.get(fieldIndex);
			}
			
			@Override
			public int getFieldIndex(String fieldName) {
				return res.indexOf(fieldName);
			}
			
			@Override
			public int getFieldCount() {
				return res.size();
			}
		};
	}

	@Nullable
	private MergedPosition getMergedPosition(Position pos) throws IOException {
		if (pos.getClass() == MergedPosition.class)
			return (MergedPosition)pos;

		int logOfPos = -1;
		for (int i = 0; i < logs.length; ++i) {
			if (logs[i] == pos.getCorrespondingLog()) {
				logOfPos = i;
				break;
			}
		}

		if (logOfPos == -1)
			return null;

		MergedPosition curPos = (MergedPosition)this.first();
		MergedPosition lastPos = (MergedPosition)this.last();

		while (	!(	curPos.cpisAscCur.first().third == logOfPos	&&
				curPos.cpisAscCur.first().first.first.equals(pos))
		) 
		{
			if (curPos.equals(lastPos)) return null;
			curPos = (MergedPosition) next(curPos);
		}

		return curPos;
	}

	private Pair<Position, Position> getLogBorders(int index) throws IOException {
		return logsBorders[index];
	}
}
