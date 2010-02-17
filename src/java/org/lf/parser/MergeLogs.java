package org.lf.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Pair;

public class MergeLogs implements Log {

	//pairs of log and record column number for merging
	private final Log[] logs;
	private final Integer[] logFields;
	
	private Position last;
	private Position first;
	
	//used for comparing record columns 
	private Comparator<String> comparator;

	
	private Comparator<Pair<Integer,Record>> pairComparator = new Comparator<Pair<Integer,Record>>() {
		@Override
		public int compare(Pair<Integer, Record> arg0,
				Pair<Integer, Record> arg1) 
		{
			int firstLogIndex = arg0.first;
			int secondLogIndex = arg1.first;
			return comparator.compare(arg0.second.get(logFields[firstLogIndex]), arg1.second.get(logFields[secondLogIndex]));
		}
	};
	
	
	private class MergedPosition implements Position {
		//this tells which log positions and records belongs to
		List<Integer> logsIndex;
		
		List<Position> positions;
		//first record is the record that will be returned by readRecord(this)
		List<Record> records;
		
		public MergedPosition(List<Position> positions, List<Integer> logsIndex, List<Record> records) {
			this.logsIndex = logsIndex;
			this.positions = positions;
			this.records = records;

		}
	}
	
	
	public MergeLogs(Log[] logs, Integer[] fields, Comparator<String> comparator) {
		this.logs = logs;
		this.logFields = fields;
		this.comparator = comparator;
	}
	
	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();
		strB.append("MergeLogs of:");
		for (Log cur : logs) {
			strB.append(cur + ", ");
		}
		return strB.substring(0, strB.length() - 2);
	}

	@Override
	public Position first() throws IOException {
		if (first != null) return first;
		
		List<Pair<Integer, Record>> nextRecordCandidates = new LinkedList<Pair<Integer, Record>>() ;
		
		List<Position> positions = new LinkedList<Position>();
		List<Record> records = new LinkedList<Record>();
		for (int i=0; i < logs.length; ++i) {
			positions.add(logs[i].first());
			Record rec = logs[i].readRecord(positions.get(i));
			records.add(rec);
			nextRecordCandidates.add(new Pair<Integer, Record>(i, rec));
		}
		Collections.sort(nextRecordCandidates, pairComparator);
		
		List<Integer> logsIndex = new LinkedList<Integer>();		
		for (Pair<Integer, Record> cur : nextRecordCandidates) {
			logsIndex.add(cur.first);
		}
		first = new MergedPosition(positions, logsIndex, records);
		return first;
	}

	@Override
	public Position last() throws IOException {
		if (last != null) return last;

		List<Pair<Integer, Record>> nextRecordCandidates = new LinkedList<Pair<Integer, Record>>() ;
		
		List<Position> positions = new LinkedList<Position>();
		List<Record> records = new LinkedList<Record>();
		for (int i=0; i < logs.length; ++i) {
			positions.add(logs[i].last());
			Record rec = logs[i].readRecord(positions.get(i));
			records.add(rec);
			nextRecordCandidates.add(new Pair<Integer, Record>(i, rec));
		}
		
		Collections.sort(nextRecordCandidates, pairComparator);
		
		
		List<Integer> logsIndex = new LinkedList<Integer>();		
		logsIndex.add(nextRecordCandidates.get(0).first);
		last = new MergedPosition(positions.subList(0, 1), logsIndex, records.subList(0, 1));
		return last; 
	}


	@Override
	public Position next(Position pos) throws IOException {
		if (pos.equals(last())) throw new IOException("can't read after eof");
		
		List<Integer> logsIndex = new LinkedList<Integer>(((MergedPosition)pos).logsIndex);
		List<Position> positions = new LinkedList<Position>(((MergedPosition)pos).positions);
		List<Record> records = new LinkedList<Record>(((MergedPosition)pos).records);
		
		Integer firstIndex = logsIndex.get(0);
		logsIndex.remove(0);
		Position firstPos = positions.get(0);
		positions.remove(0);
		records.remove(0);
		
		Log log = logs[firstIndex];
		Position newPos = log.next(firstPos);
		Record newRec = log.readRecord(newPos);
		
		for (int i = 0; i < records.size(); i++) {
			if (comparator.compare(newRec.get(logFields[firstIndex]), records.get(i).get(logFields[logsIndex.get(i)])) < 1) {
				logsIndex.add(i, firstIndex);
				positions.add(i, newPos);
				records.add(i, newRec);
				break;
			}
		}
		return new MergedPosition(positions, logsIndex, records);
	}

	@Override
	public Position prev(Position pos) throws IOException {
		if (pos.equals(first())) throw new IOException("can't read after eof");
		
		List<Integer> logsIndex = new LinkedList<Integer>(((MergedPosition)pos).logsIndex);
		List<Position> positions = new LinkedList<Position>(((MergedPosition)pos).positions);
		List<Record> records = new LinkedList<Record>(((MergedPosition)pos).records);
		
		Integer firstIndex = logsIndex.get(0);
		logsIndex.remove(0);
		Position firstPos = positions.get(0);
		positions.remove(0);
		records.remove(0);
		
		Log log = logs[firstIndex];
		Position newPos = log.next(firstPos);
		Record newRec = log.readRecord(newPos);
		
		for (int i = 0; i < records.size(); i++) {
			if (comparator.compare(newRec.get(logFields[firstIndex]), records.get(i).get(logFields[logsIndex.get(i)])) < 1) {
				logsIndex.add(i, firstIndex);
				positions.add(i, newPos);
				records.add(i, newRec);
				break;
			}
		}
		return new MergedPosition(positions, logsIndex, records);
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		return ((MergedPosition)pos).records.get(0);
	} 
	
}
