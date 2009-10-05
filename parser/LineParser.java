package parser;

import java.io.IOException;

public class LineParser implements Parser {
	/*
	 * return -1 if eof
	 * 
	*/
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		int i;
		long offset=0;
		while ( ( i = is.read() ) != (int)'\n'){
			if (i==-1) break;
			offset++;

		}
		offset++;
		if (i == -1) return -1;
		is.scrollBack(offset);
		return 	offset;
	}

	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		long scrolled  = is.scrollBack(2); 
		if (scrolled != 2) {
			is.scrollForward(scrolled);
			return -1;
		}
		
		long offset=0;
		while ( is.read() != (int)'\n'){
			scrolled = is.scrollBack(2);
			if (scrolled != 2 ){
				is.scrollForward(scrolled);
				return -1;
			}
			offset++;
		}
		
		offset++; 
		is.scrollForward(offset);
		return offset;
	}

	public Record readRecord(ScrollableInputStream is) throws IOException {
		long offset = findNextRecord(is);
		if (offset == -1) throw new IOException();
		String s = new String();
		for (long i = 0; i < offset; ++i){
			s +=(char)is.read();
		}
		
		return new Record(s);
	}

}
