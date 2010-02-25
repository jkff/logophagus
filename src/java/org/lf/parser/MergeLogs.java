package org.lf.parser;

import java.io.IOException;
import java.util.*;

import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import static org.lf.util.Comparators.inverse;

import org.lf.util.Comparators;
import org.lf.util.Pair;
import org.lf.util.Triple;

import static org.lf.util.CollectionFactory.newPriorityQueue;
import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.triple;

public class MergeLogs implements Log {
	private final Log[] logs;
	private final Integer[] timeFieldIndices;
	
	//used for comparing record columns 
	private Comparator<String> timeComparator;

    private Comparator<Triple<Integer,Position,Record>> POS_COMPARATOR = new Comparator<Triple<Integer, Position,Record>>() {
        @Override
        public int compare(Triple<Integer,Position,Record> o1, Triple<Integer,Position,Record> o2) {
        	return timeComparator.compare(
        			o1.third.get(timeFieldIndices[o1.first]),
        			o1.third.get(timeFieldIndices[o2.first]));
        }
    };

    private Comparator<Triple<Integer,Position,Record>> INVERSE_POS_COMPARATOR = new Comparator<Triple<Integer, Position,Record>>() {
        @Override
        public int compare(Triple<Integer,Position,Record> o1, Triple<Integer,Position,Record> o2) {
        	return inverse(timeComparator).compare(
        			o1.third.get(timeFieldIndices[o1.first]),
        			o1.third.get(timeFieldIndices[o2.first]));
        }
    };

    private class MergedPosition implements Position {
		PriorityQueue<Triple<Integer,Position,Record>> positions;
		PriorityQueue<Triple<Integer,Position,Record>> inversePositions;

		public MergedPosition(PriorityQueue<Triple<Integer,Position,Record>> positions,
				PriorityQueue<Triple<Integer,Position,Record>> inversePositions) {
            this.positions = positions;
            this.inversePositions = inversePositions;
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

    private PriorityQueue<Triple<Integer,Position, Record>> fromList(List<Triple<Integer,Position,Record>> positions) {
        PriorityQueue<Triple<Integer,Position,Record>> q = new PriorityQueue<Triple<Integer, Position,Record>>(
                positions.size(), POS_COMPARATOR);
        for (Triple<Integer, Position, Record> p : positions) q.offer(p);
        return q;
    }

    private PriorityQueue<Triple<Integer,Position, Record>> inverseFromList(List<Triple<Integer,Position,Record>> positions) {
        PriorityQueue<Triple<Integer,Position,Record>> q = new PriorityQueue<Triple<Integer, Position,Record>>(
                positions.size(), INVERSE_POS_COMPARATOR);
        for (Triple<Integer, Position, Record> p : positions) q.offer(p);
        return q;
    }

    
	@Override
	public Position first() throws IOException {
        List<Triple<Integer,Position,Record>> p = newList();
        for(int i = 0; i < logs.length; ++i) p.add(triple(i, logs[i].first(), logs[i].readRecord(logs[i].first())));
		return new MergedPosition(fromList(p), inverseFromList(p));
	}

	@Override
	public Position last() throws IOException {
        List<Triple<Integer,Position,Record>> p = newList();
        for(int i = 0; i < logs.length; ++i) p.add(triple(i, logs[i].last(), logs[i].readRecord(logs[i].last())));
		return new MergedPosition(fromList(p), inverseFromList(p));
	}

	@Override
	public Position next(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        Triple<Integer,Position,Record> cur = p.positions.poll();
        Position nextPos = logs[cur.first].next(cur.second);
        Triple<Integer,Position,Record> next = triple(cur.first, nextPos, logs[cur.first].readRecord(nextPos));
        PriorityQueue<Triple<Integer,Position,Record>> res = newPriorityQueue(p.positions);
        res.offer(next);
        return new MergedPosition(res);
	}

	@Override
	public Position prev(Position pos) throws IOException {
        throw new UnsupportedOperationException(";'FIXME: Add second priority queue with reverse order");
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		Triple<Integer,Position,Record> p = ((MergedPosition) pos).positions.peek();
        return p.third;
    }
}
