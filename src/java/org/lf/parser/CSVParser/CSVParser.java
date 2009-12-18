package org.lf.parser.CSVParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.lf.parser.Parser;
import org.lf.parser.Record;
import org.lf.parser.ScrollableInputStream;

public class CSVParser implements Parser {
	public static final char DEFAULT_RECORD_DELIMETER = '\n';
	public static final char DEFAULT_FIELD_DELIMETER = ',';
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    	
	private ForwardTransitionFunction ftf;
	private BackwardTransitionFunction btf;
	
    public CSVParser() {
        this(DEFAULT_RECORD_DELIMETER,DEFAULT_FIELD_DELIMETER, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVParser(char recordDelimeter, char fieldDelimeter, char quoteCharacter, char escapeCharacter) {
		ftf = new ForwardTransitionFunction(recordDelimeter, fieldDelimeter, quoteCharacter, escapeCharacter );
		btf = new BackwardTransitionFunction(recordDelimeter, fieldDelimeter, quoteCharacter, escapeCharacter );		

	}
	
    enum State {
    	BETWEEN_FIELDS,
    	FIELD,
    	RECORD_BORDER,
    	IN_QUOTE,
    	QUOTE_BEGIN,
    	QUOTE_END,
    	IN_QUOTE_ESCAPE,
    	DOUBLE_QUOTE,
    	ERROR
    }
    
    enum SymbolType {
    	RECORD_DELIMETER, 
    	FIELD_DELIMETER, 
    	QUOTE, 
    	ESCAPE,
    	OTHER
    }

    
    // TODO: Organize parsing in the form of a DFA
    // TODO: Remove code/concept duplication between "finding the next record" and "reading a record":
    // there should be only a "read a record" method, perhaps returning the number of bytes read,
    // and a "find start point of previous record" method.
        
    
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		long offset = 0;
		int c;
		State curState = State.RECORD_BORDER;
				
		do {
			c = is.read();
			if (c == -1) {
				if (offset == 0) {
					is.scrollForward(offset);
					return -1;
				}
				return offset;
			}
			++offset;
			curState = ftf.get(curState, (char)c);
//			System.out.println("offset: "+offset+";symbol: "+ c +" ; state: " + curState);
		} while (curState != State.RECORD_BORDER );
		
		is.scrollBack(offset);
		return offset;
	}

	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		int c;
		State curState = State.RECORD_BORDER;

		if (is.scrollBack(1) == 0)
			return -1;
		long offset = 0;
		
		do {
			c = readBack(is);
			++offset;
			if (c == -1) {
				if (offset == 0) {
					is.scrollForward(offset);
					return -1;
				}
				return offset;
			}
			curState = btf.get(curState, (char)c);
//			System.out.println("BTF--offset: "+offset+";symbol: "+ c +" ; state: " + curState);
			
		} while (curState != State.RECORD_BORDER );

		is.scrollForward(offset);
		return offset;
	}

	private int readBack(ScrollableInputStream is) throws IOException{
		if (is.scrollBack(1) == 0)
			return -1;
		int res = is.read();
		is.scrollBack(1);
		return res;
	}
	

	public Record readRecord(ScrollableInputStream is) throws IOException {
		long offset = 0;
		
		int curChar;
		
		State curState = State.RECORD_BORDER;
		
		CSVRecord rec = new CSVRecord();
		StringBuilder field = new StringBuilder();
		
		while ((curChar = is.read()) != -1) {
			++offset;
			curState = ftf.get(curState, (char)curChar);
			
			switch(curState) {
			case BETWEEN_FIELDS:
				rec.record.add(field.toString());
				field.delete(0, field.length());
				break;
			case ERROR:
				System.out.println("Bad cvs format:" + rec + field);
				break;
			case FIELD:
				field.append((char)curChar);
				break;
			case IN_QUOTE:
				field.append((char)curChar);
				break;				
			case IN_QUOTE_ESCAPE:
				break;
			case DOUBLE_QUOTE:
				field.append((char)curChar);
				break;
			case QUOTE_BEGIN:
				break;
			case QUOTE_END:
				break;
			case RECORD_BORDER:
				is.scrollBack(offset);
				if (field.length() > 0)
					rec.record.add(field.toString());
				return rec;
			}
		}
		
		is.scrollBack(offset);		
		if (field.length() > 0)
			rec.record.add(field.toString());
		return rec;
	}
	
	private class CSVRecord implements Record {
		List<String> record = new ArrayList<String>();
		String str = null;
		
		public String toString(){
			if (str == null) {
				StringBuilder strb = new StringBuilder();
				for (String cur : record) {
					strb.append(cur);
				}
				str = strb.toString();
			}
			
			return str;
		}

		public String get(int index) {
			return record.get(index);
		}

		public int size() {
			return record.size();
		}
	}

}
