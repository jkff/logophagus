package org.lf.parser;

import org.lf.util.Comparators;
import org.lf.util.Triple;

import com.sun.swing.internal.plaf.synth.resources.synth;

import java.io.IOException;
import java.util.*;

import static org.lf.util.CollectionFactory.newHashSet;
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
        	int res =  timeComparator.compare(
        			o1.third.get(timeFieldIndices[o1.first]),
        			o2.third.get(timeFieldIndices[o2.first]));
        	if (res == 0) {
        		if (o1.first > o2.first)
        			res = 1;
        		else 
        			res = -1;
        	}
        	return res;
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
	synchronized public Position next(Position pos) throws IOException {
        // Specification:
        // readRecord(next(p)) is the earliest record in the union of all records
        // in all logs, later than readRecord(p).

        MergedPosition p = (MergedPosition) pos;
        TreeSet<IPR> copy = new TreeSet<IPR>(p.queue);
        Iterator<IPR> i = copy.iterator();
        IPR cur = i.next();
        i.remove();
        Position nextPos = logs[cur.first].next(cur.second);
        if(nextPos != null) {
            IPR next = new IPR(cur.first, nextPos, logs[cur.first].readRecord(nextPos));
            copy.add(next);
        }

        if(copy.isEmpty())
            return null;
        return new MergedPosition(copy);
	}

	@Override
	synchronized public Position prev(Position pos) throws IOException {
        MergedPosition p = (MergedPosition) pos;
        TreeSet<IPR> copy = new TreeSet<IPR>(p.queue);
        TreeSet<IPR> sortedPrev = new TreeSet<IPR>(POS_COMPARATOR);
        for (IPR ipr : copy) {
        	Position prevPos = logs[ipr.first].prev(ipr.second);
        	if (prevPos != null) {
        		IPR prevIPR = new IPR(ipr.first, prevPos, logs[ipr.first].readRecord(prevPos));
        		sortedPrev.add(prevIPR);
        	}
		}
        
        Iterator<IPR> i = sortedPrev.descendingIterator();
        while (i.hasNext()) {
        	IPR curFromPrev = i.next();
        	Iterator<IPR> j = copy.iterator();
        	while (j.hasNext()) {
        		IPR curFromCopy = j.next();
				if (curFromCopy.first.equals(curFromPrev.first)) { 
					j.remove();
					copy.add(curFromPrev);
					if (copy.first().equals(curFromPrev))
						return new MergedPosition(copy);
					else {
						copy = new TreeSet<IPR>(p.queue);
						break;
					}
				}
			};
        }
        
        return null;
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		IPR p = ((MergedPosition) pos).queue.iterator().next();
        return p.third;
    }

	@Override
	public Field[] getFields() {
		int max=0;
		for (int i = 0; i < logs.length; ++i) {
			if (logs[max].getFields().length < logs[i].getFields().length) max = i; 
		}
		return logs[max].getFields();
	}
}
