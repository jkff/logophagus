package org.lf.parser.regex;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lf.logs.Cell;
import org.lf.logs.Record;
import org.lf.logs.Cell.Type;
import org.lf.parser.*;
import org.lf.parser.LogMetadata;

import static org.lf.util.CollectionFactory.newList;

public class RegexParser implements Parser {
	private final Pattern pattern;
	private final char recordDelimeter;
	private final LogMetadata logMetadata;

	public RegexParser(String regex, char recordDelimeter, LogMetadata logMetadata) {
		this.pattern = Pattern.compile(regex);
		this.recordDelimeter = recordDelimeter;
		this.logMetadata = logMetadata;
	}

	@Override
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		return getRecordFromCharStream(forward(is), new Sink() {
			@Override
			public void onChar(char c) {}
		});
	}

	@Override
	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		if (is.scrollBack(1) == 0)
			return 0;
		return getRecordFromCharStream(backward(is), new Sink() {
			@Override
			public void onChar(char c) {}
		});
	}



	@Override
	public Record readRecord(ScrollableInputStream is) throws IOException {
		final StringBuilder record = new StringBuilder();

		getRecordFromCharStream(forward(is), new Sink() {
			@Override
			public void onChar(char c) {
				record.append(c);
			}
		});
		return new MatcherRecord(record.toString());
	}

	private long getRecordFromCharStream(CharStream cs, Sink sink) throws IOException {
		long offset = 0;
		int c;
		do {
			c = cs.next();
			if (c == -1) return offset;
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
		private final Cell[] cells;

		private MatcherRecord(String rawRecord) {
            List<Cell> cellsList = newList();
			Matcher m = pattern.matcher(rawRecord);
			//if there is no group besides zero group then use this group(hole record) as only one field
			if (!m.matches()) 
				cellsList.add(new MatcherCell(rawRecord, "Failed to match", Type.TEXT, 0));
			else {
				//zero group is not included in groupCount() method result
				for(int i = 1; i <= m.groupCount(); ++i) {
					cellsList.add(new MatcherCell(m.group(i), logMetadata.getFieldName(i -1), Type.TEXT, i -1));
				}
			}
            cells = cellsList.toArray(new Cell[0]);
		}

		@Override
		public Cell[] getCells() {
			return cells;
		}
	}

	private static class MatcherCell extends Cell {
		private final Object value;
		private final String name;
		private final int index;
		private final Type type;
		
		public MatcherCell(Object value,  String name, Type type, int index) {
			this.value = value;
			this.name = name;
			this.type = type;
			this.index = index;
			
		}
		
		@Override
		public int getIndexInRecord() {
			return index;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
		}
		
	}
	
	@Override
	public LogMetadata getLogMetadata() {
		return logMetadata;
	}

}
