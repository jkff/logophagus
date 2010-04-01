package org.lf.parser.regex;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lf.logs.Field;
import org.lf.logs.Record;
import org.lf.parser.*;
import org.lf.util.Triple;


public class RegexParser implements Parser {
	private final Pattern[] patterns;
	private final char recordDelimeter;
	private final Field[][] regexFields;
	private final int maxLinesPerRecord;
	
	private abstract class ReadableSink extends Sink {
		public abstract String getReceivedChars();
	}

	private class OrderedReadableSink extends ReadableSink {
		private final boolean isForward;
		private final StringBuilder strB;
		public OrderedReadableSink(boolean isForward) {
			this.isForward = isForward;
			this.strB = new StringBuilder();
		}
		
		@Override
		public String getReceivedChars() {
            if(isForward)
                return strB.toString();
            StringBuilder rev = new StringBuilder();
            rev.append(strB);
            rev.reverse();
            return rev.toString();
		}

		@Override
		public void onChar(char c) {
            strB.append(c);
		}
	}

	public RegexParser(String[] regexes, Field[][] regexFields, char recordDelimeter, int maxLinesPerRecord) {
		this.patterns = new Pattern[regexes.length];
		for(int i = 0; i < regexes.length; ++i) {
			patterns[i] = Pattern.compile(regexes[i]);
		}
		
		this.regexFields = regexFields;
		this.recordDelimeter = recordDelimeter;
		this.maxLinesPerRecord = maxLinesPerRecord;
	}

	@Override
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		ReadableSink sink = new OrderedReadableSink(true);
		return getRecordFromCharStream(forward(is), sink).first;
	}

	@Override
	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		if (is.scrollBack(1) == 0)
			return 0;
		ReadableSink sink = new OrderedReadableSink(false);
		return getRecordFromCharStream(backward(is), sink).first;
	}

	@Override
	public Record readRecord(ScrollableInputStream is) throws IOException {
		ReadableSink sink = new OrderedReadableSink(true);
		Triple<Long, Integer, Matcher> offsetIndexMatch = getRecordFromCharStream(forward(is), sink);
		
		String[] cells = new String[offsetIndexMatch.second == -1 ? 1 : regexFields[offsetIndexMatch.second].length ];
		Field[] fields = (offsetIndexMatch.second.equals(-1) ? new Field[1] : regexFields[offsetIndexMatch.second]);
		if (offsetIndexMatch.second.equals(-1)) {
			cells[0] = sink.getReceivedChars();
			fields[0] = new Field("Unknown record format");
		} else {
			for (int i = 0; i < regexFields[offsetIndexMatch.second].length; ++i) {
				cells[i] = offsetIndexMatch.third.group(i + 1);
			}
		}
		for (String cell : cells) {
			System.out.print(cell);	
		}
		System.out.println("");
		return new MatcherRecord(cells, fields);
	}

	
	//Long - offset, Integer - index of pattern(-1 then no matches).
	private Triple<Long, Integer, Matcher> getRecordFromCharStream(CharStream cs, ReadableSink sink) throws IOException {
		long firstLineBreakOffset = 0;
		long offset = 0;
		for(int i = 0 ; i < maxLinesPerRecord; ++i) {
			long temp = getLineFromCharStream(cs, sink);
			if (temp == 0) 	break;
			offset += temp;
			if (i == 0)		firstLineBreakOffset = offset;
			
			for (int j = 0; j < patterns.length; ++j) {
				Matcher m = patterns[j].matcher(sink.getReceivedChars());
				if (m.matches()) 
					return new Triple<Long, Integer, Matcher>(offset, j, m);
			}
		}
		return new Triple<Long, Integer, Matcher>(firstLineBreakOffset, -1, null);
	}
	
	private long getLineFromCharStream(CharStream cs, Sink sink) throws IOException {
		long offset = 0;
		int c;
		do {
			c = cs.next();
			if (c == -1) 
                return offset;
			++offset;
			sink.onChar((char)c);
		} while ((char)c != recordDelimeter);
		return offset;
	}

	private CharStream forward(final ScrollableInputStream is) {
		return new CharStream() {
			public int next() throws IOException {
				return is.read();
			}
		};
	}

	private CharStream backward(final ScrollableInputStream is) {
		return new CharStream() {
			public int next() throws IOException {
				if (is.scrollBack(1) == 0)
					return -1;
				int res = is.read();
				is.scrollBack(1);
				return res;
			}
		};
	}

	private class MatcherRecord implements Record {
		private final String[] cells;
		private final Field[] fields;

		private MatcherRecord(String[] cells, Field[] f) {
			this.cells = cells;
			this.fields = f;
		}

		@Override
		public String[] getCellValues() {
			return cells;
		}

		@Override
		public Field[] getFields() {
			return fields;
		}

	}


}
