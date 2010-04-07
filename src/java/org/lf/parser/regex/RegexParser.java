package org.lf.parser.regex;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.RecordAdapter;
import org.lf.logs.UnknownFormat;
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

		if (offsetIndexMatch.second.equals(-1)) 
			return new RecordAdapter(new String[]{ sink.getReceivedChars() }, UnknownFormat.getInstance(1));

		final Field[] fields = regexFields[offsetIndexMatch.second];
		String[] cells = new String[regexFields[ offsetIndexMatch.second].length ];
		for (int i = 0; i < fields.length; ++i) {
			cells[i] = offsetIndexMatch.third.group(i + 1);
		}
		
		return new RecordAdapter(cells, new Format() {
			@Override
			public Field[] getFields() {
				return fields;
			}
		});
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
			++offset;			
			if (c == -1) 
				return offset;
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

	@Override
	public Format[] getFormats() {
		// TODO Auto-generated method stub
		return null;
	}


}
