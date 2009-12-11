package org.lf.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser implements Parser {
	public static final char DEFAULT_RECORD_DELIMETER = '\n';
	public static final char DEFAULT_FIELD_DELIMETER = ',';
    public static final char DEFAULT_QUOTE_CHARACTER = '"';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    
	private Character recordDelimeter; 
	private Character fieldDelimeter; 
	private Character quoteCharacter; 
	private Character escapeCharacter; 

    

    public CSVParser() {
        this(DEFAULT_RECORD_DELIMETER,DEFAULT_FIELD_DELIMETER, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVParser(char fieldDilimeter) {
        this(DEFAULT_RECORD_DELIMETER,fieldDilimeter, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVParser(char fieldDilimeter, char quoteCharacter) {
        this(DEFAULT_RECORD_DELIMETER,fieldDilimeter, quoteCharacter, DEFAULT_ESCAPE_CHARACTER);    	
    }

    public CSVParser(char fieldDilimeter, char quoteCharacter, char escapeCharacter) {
        this(DEFAULT_RECORD_DELIMETER,fieldDilimeter, quoteCharacter, escapeCharacter);    	
    }

    public CSVParser(Character recordDelimeter, Character fieldDelimeter, Character quoteCharacter, Character escapeCharacter){
		this.recordDelimeter = recordDelimeter;
		this.fieldDelimeter = fieldDelimeter;
		this.quoteCharacter = quoteCharacter;
		this.escapeCharacter = escapeCharacter;
	}
	
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		long offset = 0;
		char c;		
		do {
			c = (char)is.read();
			++offset;
			if (c == quoteCharacter) {
				offset += countForwardSymbolsInQuote(is);
				continue;
			}
		} while (c != recordDelimeter);
		
		is.scrollBack(offset);
		return offset;
	}

	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		char c;
		if (is.scrollBack(1) == 0)
			return -1;
		long offset = 1;
		
		do {
			int temp = readBack(is);
			if (temp == -1) {
				is.scrollForward(offset);
				return -1;
			}
			c = (char)temp;
			
			++offset;
			if (c == quoteCharacter) {
				offset += countBackwardSymbolsInQuote(is);
				continue;
			}
		} while (c != recordDelimeter);

		is.scrollForward(offset);
		return offset;
	}

	
	private int countForwardSymbolsInQuote(ScrollableInputStream is) throws IOException{
		int offset = 0;
		while (true) {
			char c = (char)is.read();
			offset++;
			
			if (c == escapeCharacter) {
				c = (char)is.read();
				offset++;
				continue;
			}
			
			// нужно переводить "" => "
			if (c == quoteCharacter) {
				char nextChar = (char)is.read();
				if (nextChar == quoteCharacter) {
					offset++;
					continue;
				}
				
				is.scrollBack(1);
				return offset;
			}
		}		
	}

	private int readBack(ScrollableInputStream is) throws IOException{
		if (is.scrollBack(1) == 0)
			return -1;
		int res = is.read();
		is.scrollBack(1);
		return res;
	}
	
	private int countBackwardSymbolsInQuote(ScrollableInputStream is) throws IOException{
		int offset = 0;
		
		while (true) {
			char c = (char)readBack(is);
			offset++;
			//System.out.print(c);
			// нужно переводить "" => "
			if (c == quoteCharacter) {
				char prevChar = (char)readBack(is);
				if (prevChar == quoteCharacter || prevChar == escapeCharacter) {
					offset++;
					continue;
				}
				return offset;
			}
		}		
	}

	public Record readRecord(ScrollableInputStream is) throws IOException {
		long offset = findNextRecord(is);
     	if (offset == -1)
			throw new IOException("Can't read after eof");
		byte[] buf = new byte[(int) offset];
		int n = is.read(buf);
		
		String rawRecord = new String(buf, 0, n);
//		System.out.println("record = " + rawRecord);
		CSVRecord rec = new CSVRecord();
		rec.str = rawRecord;
		
		StringBuilder field = new StringBuilder();
		for (int curPos = 0; curPos < rawRecord.length(); ++curPos){
			if (rawRecord.charAt(curPos) == quoteCharacter) {
				curPos += parseStringInQuote(rawRecord.substring(curPos+1), field);
//				System.out.println("In Quote >> " + field.toString()+"<<");
				continue;
			}
			
			if (rawRecord.charAt(curPos) == fieldDelimeter){
				rec.record.add(field.toString());
				field.delete(0, field.length());
				continue;
			}
			field.append(rawRecord.charAt(curPos));
		}
		rec.record.add(field.toString());
		return rec;
	}
	
	//читает пока не встретит quote
	//quote может быть экранирован quot-ом либо символом escapeCharacter - тогда читает дальше
	//возвращает строку соответствующую записи CVS в quote 
	private int parseStringInQuote(String in , StringBuilder result){
		boolean wasEscape = false;
//		System.out.println(in);
		int curPos;
		for (curPos = 0; curPos < in.length(); ++curPos) {
			if (wasEscape) {
				wasEscape = false;
				result.append(in.charAt(curPos));
				continue;
			}
			
			if (in.charAt(curPos) == escapeCharacter) {
				wasEscape = true;
				continue;
			}
			
			// нужно менять "" => "
			if (in.charAt(curPos) == quoteCharacter) {
				if (quoteCharacter == in.charAt(curPos+1) ) {
					++curPos;
					result.append(quoteCharacter);
					continue;
				}
//				System.out.println("return "+ result.toString());
				curPos++;
				return curPos;
			}
			result.append(in.charAt(curPos));
		}
		return curPos;
	}

	
	private class CSVRecord implements Record{
		List<String> record = new ArrayList<String>();
		String str;
		
		public String toString(){
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
