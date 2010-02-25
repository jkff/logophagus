package org.lf.parser;

import org.lf.util.Comparators;
import org.lf.util.Triple;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import static org.lf.util.CollectionFactory.newList;

public class MergeLogs implements Log {
	private final Log[] logs;
	private final Integer[] timeFieldIndices;
	
	//used for comparing record columns 
	private Comparator<String> timeComparator;

    private class IPR extends Triple<Integer,Position,Record> {
        private IPR(Integer first, Position second, Record third) {
            super(first, second, third);
        }
    }
    
    private Comparator<IPR> POS_COMPARATOR = new Comparator<IPR>() {
        @Override
        public int compare(IPR o1, IPR o2) {
        	return timeComparator.compare(
        			o1.third.get(timeFieldIndices[o1.first]),
        			o1.third.get(timeFieldIndices[o2.first]));
        }
    };

    private class MergedPosition implements Position {
		TreeSet<IPR> queue;

        private MergedPosition(TreeSet<IPR> queue) {
            this.queue = queue;
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

    private TreeSet<IPR> fromList(List<IPR> positions) {
        TreeSet<IPR> q = new TreeSet<IPR>(POS_COMPARATOR);
        for (IPR p : positions) q.add(p);
        return q;
    }

	@Override
	public Position first() throws IOException {
        List<IPR> p = newList();
        for(int i = 0; i < logs.length; ++i) p.add(new IPR(i, logs[i].first(), logs[i].readRecord(logs[i].first())));
		return new MergedPosition(fromList(p));
	}

	@Override
	public Position last() throws IOException {
        List<IPR> p = newList();
        for(int i = 0; i < logs.length; ++i) p.add(new IPR(i, logs[i].last(), logs[i].readRecord(logs[i].last())));
		return new MergedPosition(fromList(p));
	}

	@Override
	public Position next(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        TreeSet<IPR> copy = new TreeSet<IPR>(p.queue);
        Iterator<IPR> i = copy.iterator();
        IPR cur = i.next();
        i.remove();
        Position nextPos = logs[cur.first].next(cur.second);
        IPR next = new IPR(cur.first, nextPos, logs[cur.first].readRecord(nextPos));
        copy.add(next);
        return new MergedPosition(copy);
	}

	@Override
	public Position prev(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        TreeSet<IPR> copy = new TreeSet<IPR>(p.queue);
        Iterator<IPR> i = copy.descendingIterator();
        IPR cur = i.next();
        i.remove();
        Position nextPos = logs[cur.first].prev(cur.second);
        IPR next = new IPR(cur.first, nextPos, logs[cur.first].readRecord(nextPos));
        copy.add(next);
        return new MergedPosition(copy);
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		IPR p = ((MergedPosition) pos).queue.iterator().next();
        return p.third;
    }
}
