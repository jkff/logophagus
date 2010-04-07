package org.lf.parser.csv;

import java.io.IOException;
import java.util.*;

import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.logs.UnknownFormat;
import org.lf.parser.*;

import static org.lf.util.CollectionFactory.newList;

public class CSVParser implements Parser {
	private final char recordDelimeter;
	private final char fieldDelimeter;
	private final char quoteCharacter;
	private final char escapeCharacter;
	private final Field[] fields;
	private final Format csvFormat;
	
	public CSVParser() {
		this(null ,'\n', ',', '"', '\\');
	}

	public CSVParser(Field[] fields) {
		this(fields,'\n', ',', '"', '\\');
	}

	public CSVParser(Field[] fields, char recordDelimeter, char fieldDelimeter, char quoteCharacter, char escapeCharacter) {
		this.recordDelimeter = recordDelimeter;
		this.fieldDelimeter = fieldDelimeter;
		this.quoteCharacter = quoteCharacter;
		this.escapeCharacter = escapeCharacter;
		this.fields = fields;
		this.csvFormat = new Format() {
			@Override
			public Field[] getFields() {
				return CSVParser.this.fields;
			}
		};

	}

	public long findNextRecord(ScrollableInputStream is) throws IOException {
		return findBorder(forward(is), new Forward(), new Nop());
	}

	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		if (is.scrollBack(1) == 0)
			return 0;
		return findBorder(backward(is), new Backward(), new Nop());
	}


	private long findBorder(
			CharStream stream,
			TransitionFunction<State,SymbolType> tf,
			Sink sink) throws IOException
			{
		State state = State.RECORD_BORDER;
		int offset = 0;
		do {
			int i = stream.next();
			offset++;
			if (i == -1) {
				sink.recordBorder();
				return offset;
			}

			char c = (char)i;
			SymbolType s =
				(c == recordDelimeter) ? SymbolType.RECORD_DELIMITER :
					(c == escapeCharacter) ? SymbolType.ESCAPE :
						(c == fieldDelimeter)  ? SymbolType.FIELD_DELIMITER :
							(c == quoteCharacter)  ? SymbolType.QUOTE :
								SymbolType.OTHER;

			state = tf.next(state, s);

			switch(state) {
			case FIELD:             sink.onChar(c);     break;
			case IN_QUOTE:          sink.onChar(c);     break;
			case DOUBLE_QUOTE:      sink.onChar(c);     break;
			case BETWEEN_FIELDS:    sink.fieldBreak();  break;
			case ERROR:             sink.error();       break;
			case RECORD_BORDER:     sink.recordBorder();break;

			default:                                    break;
			}
		} while (state != State.RECORD_BORDER);

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

	public Record readRecord(ScrollableInputStream is) throws IOException {
		final List<String> fields = newList();

		findBorder(forward(is), new Forward(),
				new Sink() {
			private StringBuilder sb = new StringBuilder();

			public void onChar(char c) {
				sb.append(c);
			}
			public void fieldBreak() {
				fields.add(sb.toString());
				sb = new StringBuilder();
			}
			public void error() { }

			public void recordBorder() {
				fields.add(sb.toString());
			}
		});

		return new CSVRecord(fields);
	}


	private class CSVRecord implements Record {
		private final String[] cells;
		private final boolean matchesFormat;
		
		private CSVRecord(List<String> strFields) {
			matchesFormat = (CSVParser.this.fields != null )	&& 
				(strFields.size() == CSVParser.this.fields.length);

			this.cells = new String[strFields.size()];
			

			for (int i = 0; i < cells.length; i++)
				cells[i] =  strFields.get(i);
		}
		
		private Field[] getDismatchFields(List<String> strFields) {
			Field[] res = new Field[strFields.size()];
			for (int i = 0; i < cells.length; i++)
				res[i] =  new Field("Field"+i, Field.Type.TEXT);
			return res;
		}
		
		@Override
		public String[] getCellValues() {
			return cells;
		}

		@Override
		public Format getFormat() {
			return matchesFormat ? CSVParser.this.csvFormat : UnknownFormat.getInstance(cells.length);
		}
	}

	private static class Nop extends Sink {
		public void onChar(char c) { }
	}

	@Override
	public Format[] getFormats() {
		return new Format[] { csvFormat };
	}

}
