package org.lf.parser;

import java.io.IOException;
import java.util.*;

import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Comparators;
import org.lf.util.Pair;

import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.pair;

public class MergeLogs implements Log {
	//pairs of log and record column number for merging
	private final Log[] logs;
	private final Integer[] timeFieldIndices;
	
	//used for comparing record columns 
	private Comparator<String> timeComparator;

    private Comparator<Pair<Integer,Position>> POS_COMPARATOR = new Comparator<Pair<Integer, Position>>() {
        @Override
        public int compare(Pair<Integer, Position> o1, Pair<Integer, Position> o2) {
            try {
                return timeComparator.compare(
                        logs[o1.first].readRecord(o1.second).get(timeFieldIndices[o1.first]),
                        logs[o2.first].readRecord(o2.second).get(timeFieldIndices[o2.first]));
            } catch (IOException e) {
                throw new AssertionError("TODO: Use Triple<Integer,Position,Record> instead of Pair<Integer,Position>");
            }
        }
    };

    private class MergedPosition implements Position {
		PriorityQueue<Pair<Integer,Position>> positions;

		public MergedPosition(PriorityQueue<Pair<Integer,Position>> positions) {
            this.positions = positions;
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

    private PriorityQueue<Pair<Integer,Position>> fromList(List<Pair<Integer,Position>> positions) {
        PriorityQueue<Pair<Integer,Position>> q = new PriorityQueue<Pair<Integer, Position>>(
                positions.size(), POS_COMPARATOR);
        for (Pair<Integer, Position> p : positions) q.offer(p);
        return q;
    }

	@Override
	public Position first() throws IOException {
        List<Pair<Integer,Position>> p = newList();
        for(int i = 0; i < logs.length; ++i) p.add(pair(i, logs[i].first()));
		return new MergedPosition(fromList(p));
	}

	@Override
	public Position last() throws IOException {
        List<Pair<Integer,Position>> p = new ArrayList<Pair<Integer, Position>>();
        for(int i = 0; i < logs.length; ++i) p.add(pair(i, logs[i].last()));
		return new MergedPosition(fromList(p));
	}

	@Override
	public Position next(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        Pair<Integer,Position> cur = p.positions.remove();
        Pair<Integer,Position> next = new Pair<Integer, Position>(cur.first, logs[cur.first].next(cur.second));
        PriorityQueue<Pair<Integer,Position>> res = new PriorityQueue<Pair<Integer, Position>>(p.positions);
        res.offer(next);
        return new MergedPosition(res);
	}

	@Override
	public Position prev(Position pos) throws IOException {
        throw new UnsupportedOperationException("FIXME: Add second priority queue with reverse order");
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
        Pair<Integer, Position> p = ((MergedPosition) pos).positions.peek();
        return logs[p.first].readRecord(p.second);
    }
}
